package com.selfward.core.export

import com.selfward.core.graph.GraphEdge
import com.selfward.core.graph.GraphNode
import java.util.Locale

/**
 * Serialises the knowledge graph to formats other tools can open.
 *
 * Two are produced, matching iOS `GraphExportService`:
 *
 * - **Cytoscape JSON** (`knowledge-graph.json`), which Cytoscape.js reads
 *   directly and which is shaped closely enough for a Neo4j import.
 * - **GraphML** (`knowledge-graph.graphml`), which Gephi and yEd open.
 *
 * Unlike iOS there is no aggregation step. There, each session owns its own
 * graph and export has to merge them by `(type, label)`; here the graph is
 * already one continuous structure across every session, deduplicated by label
 * as it is written, so the persisted node ids are exported as they stand.
 *
 * Everything in a label is text the person typed, so both writers escape rather
 * than interpolate. A label containing a quote or an angle bracket must not be
 * able to end an attribute early and corrupt the file — see [jsonEscape] and
 * [xmlEscape].
 */
object GraphExport {

    const val JSON_FILENAME = "knowledge-graph.json"
    const val GRAPHML_FILENAME = "knowledge-graph.graphml"

    /** The kind written when a node was stored without one. */
    private const val UNTYPED = "concept"

    /** The relation written when an edge was stored without a label. */
    private const val UNLABELLED_RELATION = "related"

    private const val DEFAULT_WEIGHT = 1.0f

    /**
     * A Cytoscape.js `elements` document.
     *
     * ```json
     * {"elements":{"nodes":[{"data":{"id":"n_1","label":"…","type":"…","strength":1.00}}],
     *              "edges":[{"data":{"id":"e_2","source":"n_1","target":"n_3","type":"…","weight":1.00}}]}}
     * ```
     */
    fun cytoscapeJson(nodes: List<GraphNode>, edges: List<GraphEdge>): String {
        val nodeItems = nodes.joinToString(",") { node ->
            """{"data":{"id":"${jsonEscape(node.id)}",""" +
                """"label":"${jsonEscape(node.label)}",""" +
                """"type":"${jsonEscape(node.kind ?: UNTYPED)}",""" +
                """"strength":${decimal(node.strength, 2)}}}"""
        }
        val edgeItems = connectedEdges(nodes, edges).joinToString(",") { edge ->
            """{"data":{"id":"${jsonEscape(edge.id)}",""" +
                """"source":"${jsonEscape(edge.sourceId)}",""" +
                """"target":"${jsonEscape(edge.targetId)}",""" +
                """"type":"${jsonEscape(edge.label ?: UNLABELLED_RELATION)}",""" +
                """"weight":${decimal(edge.weight ?: DEFAULT_WEIGHT, 2)}}}"""
        }
        return """{"elements":{"nodes":[$nodeItems],"edges":[$edgeItems]}}"""
    }

    /**
     * A GraphML document. The attribute keys are the same ones iOS declares, so
     * a file exported from either phone opens the same way:
     * `d0` label, `d1` type, `d2` strength, `d3` first seen, `d4` edge type,
     * `d5` edge weight.
     */
    fun graphML(nodes: List<GraphNode>, edges: List<GraphEdge>): String {
        val lines = mutableListOf<String>()
        lines += """<?xml version="1.0" encoding="UTF-8"?>"""
        lines += """<graphml xmlns="http://graphml.graphdrawing.org/graphml""""
        lines += """  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance""""
        lines += """  xsi:schemaLocation="http://graphml.graphdrawing.org/graphml """ +
            """http://graphml.graphdrawing.org/graphml/graphml.xsd">"""
        lines += """  <key id="d0" for="node" attr.name="label"     attr.type="string"/>"""
        lines += """  <key id="d1" for="node" attr.name="type"      attr.type="string"/>"""
        lines += """  <key id="d2" for="node" attr.name="strength"  attr.type="double"/>"""
        lines += """  <key id="d3" for="node" attr.name="firstSeen" attr.type="long"/>"""
        lines += """  <key id="d4" for="edge" attr.name="type"      attr.type="string"/>"""
        lines += """  <key id="d5" for="edge" attr.name="weight"    attr.type="double"/>"""
        lines += """  <graph id="G" edgedefault="directed">"""

        nodes.forEach { node ->
            lines += """    <node id="${xmlEscape(node.id)}">"""
            lines += """      <data key="d0">${xmlEscape(node.label)}</data>"""
            lines += """      <data key="d1">${xmlEscape(node.kind ?: UNTYPED)}</data>"""
            lines += """      <data key="d2">${decimal(node.strength, 4)}</data>"""
            lines += """      <data key="d3">${node.createdAt}</data>"""
            lines += """    </node>"""
        }

        connectedEdges(nodes, edges).forEach { edge ->
            lines += """    <edge id="${xmlEscape(edge.id)}" """ +
                """source="${xmlEscape(edge.sourceId)}" target="${xmlEscape(edge.targetId)}">"""
            lines += """      <data key="d4">${xmlEscape(edge.label ?: UNLABELLED_RELATION)}</data>"""
            lines += """      <data key="d5">${decimal(edge.weight ?: DEFAULT_WEIGHT, 4)}</data>"""
            lines += """    </edge>"""
        }

        lines += "  </graph>"
        lines += "</graphml>"
        return lines.joinToString("\n")
    }

    /**
     * Edges both of whose endpoints are being written.
     *
     * An edge pointing at a node that is not in the file is not a graph a reader
     * can load: Gephi rejects the document outright rather than skipping the
     * edge. Persisted edges outlive nothing today, but this is the cheap guard
     * against exporting a file that will not open.
     */
    private fun connectedEdges(nodes: List<GraphNode>, edges: List<GraphEdge>): List<GraphEdge> {
        val known = nodes.mapTo(HashSet()) { it.id }
        return edges.filter { it.sourceId in known && it.targetId in known }
    }

    /**
     * Formats to a fixed number of decimal places in [Locale.ROOT].
     *
     * The locale is not incidental. `String.format("%.2f", 1.5f)` on a phone set
     * to German produces "1,50", which is neither valid JSON nor a valid
     * GraphML double — the export would be silently unreadable for anyone
     * outside an English-speaking locale, and would look fine to anyone testing
     * in one.
     */
    private fun decimal(value: Float, places: Int): String =
        String.format(Locale.ROOT, "%.${places}f", value)

    /**
     * Escapes a string for a JSON string literal, including the control
     * characters below 0x20 that must be escaped for the document to parse.
     */
    private fun jsonEscape(raw: String): String = buildString(raw.length) {
        raw.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                else ->
                    if (char < ' ') append(String.format(Locale.ROOT, "\\u%04x", char.code))
                    else append(char)
            }
        }
    }

    /**
     * Escapes a string for XML text or an attribute value.
     *
     * Control characters are dropped rather than escaped: XML 1.0 forbids them
     * outright, and `&#1;` is as illegal as the raw byte. A stray control
     * character in a label would otherwise produce a file no parser will accept.
     */
    private fun xmlEscape(raw: String): String = buildString(raw.length) {
        raw.forEach { char ->
            when {
                char == '&' -> append("&amp;")
                char == '<' -> append("&lt;")
                char == '>' -> append("&gt;")
                char == '"' -> append("&quot;")
                char == '\'' -> append("&apos;")
                char == '\t' || char == '\n' || char == '\r' -> append(char)
                char < ' ' -> Unit
                else -> append(char)
            }
        }
    }
}
