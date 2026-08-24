package com.selfward.core.voice

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsRequestTest {

    @Test
    fun bodyContainsCoreFields() {
        val req = TtsRequest(input = "Hello world", voice = "nova")
        val json = req.toRequestBody()
        assertTrue(json.contains("\"input\":\"Hello world\""))
        assertTrue(json.contains("\"voice\":\"nova\""))
        assertTrue(json.contains("\"model\":\"tts-1\""))
        assertTrue(json.contains("\"response_format\":\"mp3\""))
    }

    @Test
    fun escapesQuotesInInput() {
        val req = TtsRequest(input = "say \"hi\"")
        assertTrue(req.toRequestBody().contains("say \\\"hi\\\""))
    }

    @Test
    fun roundTripsViaJson() {
        val req = TtsRequest(input = "x", voice = "onyx", speed = 1.2f)
        val decoded = Json.decodeFromString(TtsRequest.serializer(), req.toRequestBody())
        assertEquals(req, decoded)
    }

    @Test
    fun voiceCatalogKnowsOpenAiVoices() {
        assertTrue(VoiceCatalog.isKnown("alloy"))
        assertTrue(VoiceCatalog.isKnown("shimmer"))
        assertEquals(6, VoiceCatalog.openAiVoices.size)
    }
}
