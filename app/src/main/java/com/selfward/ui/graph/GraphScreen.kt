package com.selfward.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfward.core.graph.GraphNode
import com.selfward.core.graph.MessageAnalyzer

@Composable
fun GraphScreen(viewModel: GraphViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    // The graph grows while the user is on the chat tab, so recompute on return
    // rather than showing whatever was true when this screen was first created.
    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Insights", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "Drawn from your own messages on this device. Nothing here is sent anywhere.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))

        val insights = state.insights
        if (state.isEmpty || insights == null) {
            Text(
                if (state.loading) "Looking over what you've written…"
                else "Nothing yet. As you talk about how you're feeling and who's involved, " +
                    "the patterns will build up here.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("insightsEmpty")
            )
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (insights.highlights.isNotEmpty()) {
                item {
                    Section("What stands out") {
                        insights.highlights.forEach {
                            Text("• $it", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            if (insights.repeatingLoops.isNotEmpty()) {
                item {
                    Section("Patterns that repeat") {
                        insights.repeatingLoops.forEach {
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            insights.shadowObservation,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            item {
                Section("Beliefs underneath") {
                    Text(insights.lifestyleObservation, style = MaterialTheme.typography.bodyMedium)
                }
            }
            item {
                Section("Something to practise") {
                    Text(insights.skillSuggestion, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (insights.frameworkAnalysis.isNotEmpty()) {
                item {
                    Section("In this approach") {
                        Text(
                            insights.frameworkAnalysis,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            item {
                Section("The map") {
                    GraphCanvas(state.nodes, state.edges)
                }
            }
            items(state.nodes.sortedByDescending { it.strength }, key = { it.id }) { node ->
                NodeRow(node)
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun NodeRow(node: GraphNode) {
    Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp)) {
            Text(node.label, style = MaterialTheme.typography.bodyMedium)
            Text(
                kindLabel(node.kind) + timesLabel(node.strength),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun kindLabel(kind: String?): String = when (kind) {
    MessageAnalyzer.Kind.PERSON -> "Person"
    MessageAnalyzer.Kind.EMOTION -> "Feeling"
    MessageAnalyzer.Kind.BELIEF -> "Belief"
    MessageAnalyzer.Kind.THEME -> "Theme"
    "insight" -> "Reflection"
    else -> "Note"
}

/**
 * Strength is a weight, not a tally, so it is described rather than printed —
 * showing "1.5" would invite the reader to treat it as a count of something.
 */
private fun timesLabel(strength: Float): String =
    if (strength > GraphNode.BASE_STRENGTH) " · comes up often" else ""

@Composable
private fun GraphCanvas(
    nodes: List<GraphNode>,
    edges: List<com.selfward.core.graph.GraphEdge>
) {
    if (nodes.isEmpty()) return
    val shown = nodes.sortedByDescending { it.strength }.take(MAX_DRAWN)
    val shownIds = shown.map { it.id }.toSet()

    // Read from the theme out here: the canvas draw scope is not composable, and
    // the labels used to be painted with a hardcoded black that vanished
    // completely against a dark background.
    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val edgeColor = MaterialTheme.colorScheme.outline

    Canvas(Modifier.fillMaxWidth().height(260.dp).padding(8.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2
        val radius = minOf(size.width, size.height) / 2.6f
        val positions = shown.mapIndexed { i, n ->
            val angle = (2 * Math.PI * i / shown.size) - Math.PI / 2
            n.id to Offset(
                cx + radius * Math.cos(angle).toFloat(),
                cy + radius * Math.sin(angle).toFloat()
            )
        }.toMap()

        edges.forEach { e ->
            if (e.sourceId !in shownIds || e.targetId !in shownIds) return@forEach
            val s = positions[e.sourceId] ?: return@forEach
            val t = positions[e.targetId] ?: return@forEach
            drawLine(edgeColor, s, t, strokeWidth = ((e.weight ?: 1f) * 1.5f).dp.toPx())
        }
        shown.forEach { node ->
            val p = positions[node.id] ?: return@forEach
            drawCircle(colorFor(node.kind), (node.strength * 9f).dp.toPx(), p)
        }

        val paint = android.graphics.Paint().apply {
            color = labelColor
            textSize = 26f
            isAntiAlias = true
        }
        val native = drawContext.canvas.nativeCanvas
        shown.forEach { node ->
            val p = positions[node.id] ?: return@forEach
            native.drawText(node.label.take(18), p.x - 40f, p.y + 36f, paint)
        }
    }
}

/** Beyond this the ring is unreadable, so the heaviest nodes are drawn. */
private const val MAX_DRAWN = 14

private fun colorFor(kind: String?): Color = when (kind) {
    MessageAnalyzer.Kind.PERSON -> Color(0xFF4A90D9)
    MessageAnalyzer.Kind.EMOTION -> Color(0xFFD0021B)
    MessageAnalyzer.Kind.BELIEF -> Color(0xFF7ED321)
    MessageAnalyzer.Kind.THEME -> Color(0xFF9B59B6)
    else -> Color(0xFF999999)
}
