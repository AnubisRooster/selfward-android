package com.selfward.data.catalog

import androidx.test.core.app.ApplicationProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Driven through a real Ktor engine rather than a fake catalogue, because the
 * parsing is the part that breaks: OpenRouter returns pricing as strings on some
 * rows and numbers on others, and entries appear with fields missing entirely.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HttpOpenRouterCatalogTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    /** Shaped like a real /models payload, including the awkward rows. */
    private val body = """
        {"data":[
          {"id":"meta-llama/llama-3.2-3b-instruct:free","name":"Llama 3.2 3B",
           "pricing":{"prompt":"0","completion":"0"},"context_length":131072},
          {"id":"vendor/priced","name":"Priced",
           "pricing":{"prompt":"0.0000006","completion":"0.0000018"},"context_length":200000},
          {"id":"vendor/zero-as-number","name":"Zero As Number",
           "pricing":{"prompt":0,"completion":0},"context_length":8192},
          {"id":"vendor/no-pricing","name":"No Pricing","context_length":4096},
          {"id":"vendor/no-context","pricing":{"prompt":"0","completion":"0"}},
          {"name":"missing an id","pricing":{"prompt":"0","completion":"0"}}
        ]}
    """.trimIndent()

    private fun catalog(
        engine: MockEngine,
        now: () -> Long = { 1_000L }
    ) = HttpOpenRouterCatalog(HttpClient(engine), context, now)

    private fun jsonEngine(payload: String = body, record: ((String?) -> Unit)? = null) =
        MockEngine { request ->
            record?.invoke(request.headers[HttpHeaders.Authorization])
            respond(payload, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }

    @Test
    fun parsesTheCatalogueAndSkipsRowsWithoutAnId() = runBlocking {
        val models = catalog(jsonEngine()).models()

        assertEquals(
            listOf(
                "meta-llama/llama-3.2-3b-instruct:free",
                "vendor/priced",
                "vendor/zero-as-number",
                "vendor/no-pricing",
                "vendor/no-context"
            ),
            models.map { it.id }
        )
    }

    /** A price sent as a JSON number is as free as one sent as a string. */
    @Test
    fun pricingIsReadWhetherItArrivesAsStringOrNumber() = runBlocking {
        val byId = catalog(jsonEngine()).models().associateBy { it.id }

        assertTrue(byId.getValue("meta-llama/llama-3.2-3b-instruct:free").isFree)
        assertTrue(byId.getValue("vendor/zero-as-number").isFree)
        assertFalse(byId.getValue("vendor/priced").isFree)
    }

    /** Missing pricing is unknown, and unknown must not be sold as free. */
    @Test
    fun anEntryWithNoPricingIsNotFree() = runBlocking {
        val model = catalog(jsonEngine()).models().first { it.id == "vendor/no-pricing" }

        assertFalse(model.isFree)
    }

    @Test
    fun aMissingContextLengthBecomesZeroRatherThanFailingTheRow() = runBlocking {
        val model = catalog(jsonEngine()).models().first { it.id == "vendor/no-context" }

        assertEquals(0, model.contextLength)
    }

    @Test
    fun theKeyIsSentWhenThereIsOne() = runBlocking {
        var seen: String? = null
        catalog(jsonEngine(record = { seen = it })).models(apiKey = "sk-or-test")

        assertEquals("Bearer sk-or-test", seen)
    }

    /** The endpoint is public, so no key must still return a catalogue. */
    @Test
    fun noKeyStillFetches() = runBlocking {
        var seen: String? = "unset"
        val models = catalog(jsonEngine(record = { seen = it })).models(apiKey = null)

        assertEquals(null, seen)
        assertTrue(models.isNotEmpty())
    }

    /**
     * Settings must still open when OpenRouter is unreachable. An empty list
     * sends the caller to the pinned fallback; an exception would take the
     * screen down with it.
     */
    @Test
    fun aFailedRequestYieldsNothingRatherThanThrowing() = runBlocking {
        val models = catalog(MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }).models()

        assertTrue(models.isEmpty())
    }

    @Test
    fun malformedJsonYieldsNothingRatherThanThrowing() = runBlocking {
        val models = catalog(jsonEngine("not json at all")).models()

        assertTrue(models.isEmpty())
    }

    /** Stale beats empty: a cached list still lets someone choose. */
    @Test
    fun aLaterFailureFallsBackToWhatWasCached() = runBlocking {
        val store = HttpOpenRouterCatalog(HttpClient(jsonEngine()), context) { 1_000L }
        assertTrue(store.models().isNotEmpty())

        val offline = HttpOpenRouterCatalog(
            HttpClient(MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }),
            context
        ) { 1_000L + TWO_DAYS }

        assertTrue(offline.models().isNotEmpty())
    }

    @Test
    fun aFreshCacheIsServedWithoutCallingOut() = runBlocking {
        var calls = 0
        val engine = MockEngine {
            calls += 1
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val fresh = HttpOpenRouterCatalog(HttpClient(engine), context) { 5_000L }

        fresh.models()
        fresh.models()

        assertEquals("the second read should have come from cache", 1, calls)
    }

    @Test
    fun aStaleCacheIsRefetched() = runBlocking {
        var calls = 0
        val engine = MockEngine {
            calls += 1
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        var clock = 10_000L
        val catalog = HttpOpenRouterCatalog(HttpClient(engine), context) { clock }

        catalog.models()
        clock += TWO_DAYS
        catalog.models()

        assertEquals(2, calls)
    }

    @Test
    fun forcingARefreshIgnoresAFreshCache() = runBlocking {
        var calls = 0
        val engine = MockEngine {
            calls += 1
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val catalog = HttpOpenRouterCatalog(HttpClient(engine), context) { 20_000L }

        catalog.models()
        catalog.models(forceRefresh = true)

        assertEquals(2, calls)
    }

    private companion object {
        const val TWO_DAYS = 2L * 24 * 60 * 60 * 1000
    }
}
