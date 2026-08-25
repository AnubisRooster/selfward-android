package com.selfward.data.catalog

import android.content.Context
import com.selfward.core.catalog.OpenRouterCatalog
import com.selfward.core.catalog.OpenRouterModel
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Fetches OpenRouter's public model catalogue and keeps a copy on disk.
 *
 * The cache is plain SharedPreferences rather than the encrypted store: this is
 * a public price list, not a secret, and putting it behind the Keystore would
 * cost a decrypt on every settings visit for no privacy gained. The API key is
 * never written here — it is only ever passed through as a header.
 *
 * Parsed by hand from the JSON tree rather than with @Serializable classes
 * because the pricing fields arrive as strings in some entries and numbers in
 * others, and a strict decoder rejects the whole payload over one odd row.
 */
class HttpOpenRouterCatalog(
    private val client: HttpClient,
    context: Context,
    private val now: () -> Long = System::currentTimeMillis
) : OpenRouterCatalog {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun models(apiKey: String?, forceRefresh: Boolean): List<OpenRouterModel> {
        if (!forceRefresh && isFresh()) {
            cached().takeIf { it.isNotEmpty() }?.let { return it }
        }
        // Stale beats empty: an out-of-date list still lets someone pick a
        // model, where an empty one leaves them typing a slug from memory.
        val (models, body) = runCatching { fetch(apiKey) }.getOrNull() ?: return cached()
        prefs.edit()
            .putString(KEY_BODY, body)
            .putLong(KEY_FETCHED_AT, now())
            .apply()
        return models
    }

    override fun cached(): List<OpenRouterModel> {
        val body = prefs.getString(KEY_BODY, null) ?: return emptyList()
        return runCatching { parse(body) }.getOrDefault(emptyList())
    }

    private fun isFresh(): Boolean = now() - prefs.getLong(KEY_FETCHED_AT, 0L) < MAX_AGE_MILLIS

    private suspend fun fetch(apiKey: String?): Pair<List<OpenRouterModel>, String>? {
        val response: HttpResponse = client.get(MODELS_URL) {
            if (!apiKey.isNullOrBlank()) header("Authorization", "Bearer $apiKey")
        }
        if (!response.status.isSuccess()) return null
        val body = response.bodyAsText()
        val parsed = parse(body)
        return if (parsed.isEmpty()) null else parsed to body
    }

    private fun parse(body: String): List<OpenRouterModel> {
        val root = json.parseToJsonElement(body).jsonObject
        val data = root["data"]?.jsonArray ?: return emptyList()
        return data.mapNotNull { element ->
            runCatching { element.jsonObject.toModel() }.getOrNull()
        }
    }

    private fun JsonObject.toModel(): OpenRouterModel? {
        val id = this["id"]?.jsonPrimitive?.contentOrNullSafe() ?: return null
        val pricing = this["pricing"]?.jsonObject
        val architecture = this["architecture"]?.jsonObject
        return OpenRouterModel(
            id = id,
            name = this["name"]?.jsonPrimitive?.contentOrNullSafe() ?: id,
            // Absent pricing is treated as unknown, and unknown is not free.
            promptPrice = pricing?.get("prompt")?.jsonPrimitive?.contentOrNullSafe() ?: "",
            completionPrice = pricing?.get("completion")?.jsonPrimitive?.contentOrNullSafe() ?: "",
            contextLength = this["context_length"]?.jsonPrimitive?.contentOrNullSafe()
                ?.toDoubleOrNull()?.toInt() ?: 0,
            inputModalities = architecture?.modalities("input_modalities") ?: listOf("text"),
            // Absent output modality is assumed to be text, matching every
            // ordinary chat entry; the odd ones out declare themselves.
            outputModalities = architecture?.modalities("output_modalities") ?: listOf("text")
        )
    }

    private fun JsonObject.modalities(key: String): List<String>? =
        runCatching { this[key]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNullSafe() } }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }

    private fun kotlinx.serialization.json.JsonPrimitive.contentOrNullSafe(): String? =
        runCatching { content }.getOrNull()?.takeIf { it != "null" }

    private companion object {
        const val MODELS_URL = "https://openrouter.ai/api/v1/models"
        const val PREFS = "openrouter_catalog"
        const val KEY_BODY = "models_body"
        const val KEY_FETCHED_AT = "models_fetched_at"
        const val MAX_AGE_MILLIS = 24L * 60 * 60 * 1000
    }
}
