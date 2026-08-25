package com.selfward.core.export

import com.selfward.core.graph.GraphEdge
import com.selfward.core.graph.GraphNode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

/**
 * These parse the exported documents rather than searching them for substrings.
 * The whole value of an export is that another program can open it, so the
 * assertion has to be that a parser accepts it — a `contains("<node")` check
 * would pass just as happily on a file Gephi refuses to load.
 */
class GraphExportTest {

    private val originalLocale: Locale = Locale.getDefault()

    @After
    fun restoreLocale() {
        Locale.setDefault(originalLocale)
    }

    private fun node(
        id: String,
        label: String,
        kind: String? = "emotion",
        strength: Float = 1.0f,
        createdAt: Long = 1_700_000_000_000L
    ) = GraphNode(id, label, kind, createdAt, strength)

    private fun edge(
        id: String,
        source: String,
        target: String,
        label: String? = "relates",
        weight: Float? = 1.0f
    ) = GraphEdge(id, source, target, label, weight)

    private fun parseJson(text: String): JsonObject =
        Json.parseToJsonElement(text).jsonObject

    private fun parseXml(text: String): Document =
        DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(ByteArrayInputStream(text.toByteArray()))

    private fun JsonObject.nodes(): JsonArray =
        this["elements"]!!.jsonObject["nodes"]!!.jsonArray

    private fun JsonObject.edges(): JsonArray =
        this["elements"]!!.jsonObject["edges"]!!.jsonArray

    // MARK: - Shape

    @Test
    fun everyNodeAndEdgeReachesTheJson() {
        val json = parseJson(
            GraphExport.cytoscapeJson(
                listOf(node("n_1", "Mother"), node("n_2", "guilt")),
                listOf(edge("e_3", "n_1", "n_2"))
            )
        )

        assertEquals(2, json.nodes().size)
        assertEquals(1, json.edges().size)
        val first = json.nodes()[0].jsonObject["data"]!!.jsonObject
        assertEquals("n_1", first["id"]!!.jsonPrimitive.content)
        assertEquals("Mother", first["label"]!!.jsonPrimitive.content)
    }

    @Test
    fun everyNodeAndEdgeReachesTheGraphMl() {
        val doc = parseXml(
            GraphExport.graphML(
                listOf(node("n_1", "Mother"), node("n_2", "guilt")),
                listOf(edge("e_3", "n_1", "n_2"))
            )
        )

        assertEquals(2, doc.getElementsByTagName("node").length)
        assertEquals(1, doc.getElementsByTagName("edge").length)
    }

    @Test
    fun anEmptyGraphIsStillAValidDocument() {
        assertEquals(0, parseJson(GraphExport.cytoscapeJson(emptyList(), emptyList())).nodes().size)
        assertEquals(0, parseXml(GraphExport.graphML(emptyList(), emptyList()))
            .getElementsByTagName("node").length)
    }

    // MARK: - Escaping
    //
    // Labels are the person's own words, so every one of these is text a real
    // session could produce.

    @Test
    fun aQuoteInALabelDoesNotBreakTheJson() {
        val json = parseJson(
            GraphExport.cytoscapeJson(listOf(node("n_1", """she said "you're fine"""")), emptyList())
        )

        assertEquals(
            """she said "you're fine"""",
            json.nodes()[0].jsonObject["data"]!!.jsonObject["label"]!!.jsonPrimitive.content
        )
    }

    @Test
    fun aBackslashInALabelDoesNotBreakTheJson() {
        val json = parseJson(
            GraphExport.cytoscapeJson(listOf(node("n_1", """work\life balance""")), emptyList())
        )

        assertEquals(
            """work\life balance""",
            json.nodes()[0].jsonObject["data"]!!.jsonObject["label"]!!.jsonPrimitive.content
        )
    }

    @Test
    fun newlinesAndTabsSurviveTheJsonRoundTrip() {
        val json = parseJson(
            GraphExport.cytoscapeJson(listOf(node("n_1", "one\ttwo\nthree")), emptyList())
        )

        assertEquals(
            "one\ttwo\nthree",
            json.nodes()[0].jsonObject["data"]!!.jsonObject["label"]!!.jsonPrimitive.content
        )
    }

    /** A raw control character is not legal inside a JSON string literal. */
    @Test
    fun aControlCharacterIsEscapedRatherThanEmitted() {
        val json = parseJson(
            GraphExport.cytoscapeJson(listOf(node("n_1", "bell\u0007here")), emptyList())
        )

        assertEquals(
            "bell\u0007here",
            json.nodes()[0].jsonObject["data"]!!.jsonObject["label"]!!.jsonPrimitive.content
        )
    }

    @Test
    fun angleBracketsInALabelDoNotBreakTheGraphMl() {
        val doc = parseXml(
            GraphExport.graphML(listOf(node("n_1", "</node><node id='x'/>")), emptyList())
        )

        assertEquals(1, doc.getElementsByTagName("node").length)
        assertEquals(
            "</node><node id='x'/>",
            doc.getElementsByTagName("data").item(0).textContent
        )
    }

    @Test
    fun ampersandsAndQuotesInALabelDoNotBreakTheGraphMl() {
        val doc = parseXml(
            GraphExport.graphML(listOf(node("n_1", """Mum & Dad said "no"""")), emptyList())
        )

        val label = doc.getElementsByTagName("data").item(0).textContent
        assertEquals("""Mum & Dad said "no"""", label)
    }

    /**
     * XML 1.0 has no way to represent these at all — not raw, and not as a
     * numeric entity — so they have to be dropped or the file will not parse.
     */
    @Test
    fun aControlCharacterIsDroppedFromTheGraphMl() {
        val doc = parseXml(
            GraphExport.graphML(listOf(node("n_1", "bell\u0007here")), emptyList())
        )

        assertEquals("bellhere", doc.getElementsByTagName("data").item(0).textContent)
    }

    // MARK: - Locale

    /**
     * `String.format("%.2f", …)` follows the default locale, so on a phone set
     * to German it writes "1,50". That is not a JSON number and not a GraphML
     * double: the file would be unopenable for everyone outside an
     * English-speaking locale, and would look perfect to anyone testing in one.
     */
    @Test
    fun theJsonIsValidOnAPhoneSetToACommaDecimalLocale() {
        Locale.setDefault(Locale.GERMANY)

        val json = parseJson(
            GraphExport.cytoscapeJson(listOf(node("n_1", "grief", strength = 1.5f)), emptyList())
        )

        assertEquals(
            1.5,
            json.nodes()[0].jsonObject["data"]!!.jsonObject["strength"]!!.jsonPrimitive.content
                .toDouble(),
            0.001
        )
    }

    @Test
    fun theGraphMlIsValidOnAPhoneSetToACommaDecimalLocale() {
        Locale.setDefault(Locale.GERMANY)

        val doc = parseXml(
            GraphExport.graphML(
                listOf(node("n_1", "grief", strength = 1.5f)),
                emptyList()
            )
        )

        val strength = (doc.getElementsByTagName("node").item(0) as Element)
            .getElementsByTagName("data").item(2).textContent

        assertEquals(1.5, strength.toDouble(), 0.001)
    }

    // MARK: - Missing values and orphans

    @Test
    fun aNodeWithNoKindIsGivenOne() {
        val json = parseJson(
            GraphExport.cytoscapeJson(listOf(node("n_1", "something", kind = null)), emptyList())
        )

        assertEquals(
            "concept",
            json.nodes()[0].jsonObject["data"]!!.jsonObject["type"]!!.jsonPrimitive.content
        )
    }

    @Test
    fun anEdgeWithNoWeightIsGivenOne() {
        val json = parseJson(
            GraphExport.cytoscapeJson(
                listOf(node("n_1", "a"), node("n_2", "b")),
                listOf(edge("e_3", "n_1", "n_2", label = null, weight = null))
            )
        )

        val data = json.edges()[0].jsonObject["data"]!!.jsonObject
        assertEquals("related", data["type"]!!.jsonPrimitive.content)
        assertEquals(1.0, data["weight"]!!.jsonPrimitive.content.toDouble(), 0.001)
    }

    /**
     * Gephi rejects a document containing an edge to a node it has not seen,
     * rather than skipping that edge, so one dangling reference costs the whole
     * export.
     */
    @Test
    fun anEdgeToAMissingNodeIsLeftOut() {
        val nodes = listOf(node("n_1", "a"))
        val edges = listOf(edge("e_2", "n_1", "n_missing"), edge("e_3", "n_gone", "n_1"))

        assertEquals(0, parseJson(GraphExport.cytoscapeJson(nodes, edges)).edges().size)
        assertEquals(
            0,
            parseXml(GraphExport.graphML(nodes, edges)).getElementsByTagName("edge").length
        )
    }

    @Test
    fun theGraphMlDeclaresTheKeysItsDataElementsUse() {
        val doc = parseXml(
            GraphExport.graphML(
                listOf(node("n_1", "a")),
                emptyList()
            )
        )

        val declared = (0 until doc.getElementsByTagName("key").length)
            .map { (doc.getElementsByTagName("key").item(it) as Element).getAttribute("id") }

        assertTrue(declared.containsAll(listOf("d0", "d1", "d2", "d3", "d4", "d5")))
    }
}
