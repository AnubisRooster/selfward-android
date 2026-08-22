package com.theraipist.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun GraphScreen(viewModel: GraphViewModel = hiltViewModel()) {
    val nodes by viewModel.nodes.collectAsState()
    val edges by viewModel.edges.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Memory & Insights", style = MaterialTheme.typography.headlineSmall)
        if (nodes.isEmpty()) {
            Text(
                "No insights yet — start a conversation and they'll appear here as a graph.",
                style = MaterialTheme.typography.bodySmall
            )
            return@Column
        }
        Canvas(Modifier.fillMaxWidth().weight(1f).padding(8.dp)) {
            val cx = size.width / 2
            val cy = size.height / 2
            val radius = minOf(size.width, size.height) / 2.6f
            val positions = nodes.mapIndexed { i, n ->
                val angle = (2 * Math.PI * i / nodes.size) - Math.PI / 2
                n.id to Offset(
                    cx + radius * Math.cos(angle).toFloat(),
                    cy + radius * Math.sin(angle).toFloat()
                )
            }.toMap()

            val nodeColor = Color(0xFF5C6BC0)
            val edgeColor = Color.Gray
            edges.forEach { e ->
                val s = positions[e.sourceId] ?: return@forEach
                val t = positions[e.targetId] ?: return@forEach
                drawLine(edgeColor, s, t, strokeWidth = 2.dp.toPx())
            }
            positions.values.forEach { drawCircle(nodeColor, 18.dp.toPx(), it) }

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 28f
                isAntiAlias = true
            }
            val native = drawContext.canvas.nativeCanvas
            positions.forEach { (id, p) ->
                val label = nodes.first { it.id == id }.label
                native.drawText(label.take(22), p.x - 40f, p.y + 38f, paint)
            }
        }
    }
}
