package com.selfward.data.catalog

import android.content.Context
import com.selfward.core.catalog.ModelChoice
import com.selfward.core.catalog.ModelRanking
import com.selfward.core.catalog.OpenRouterCatalog
import com.selfward.core.catalog.PriceTiers
import com.selfward.core.catalog.ProviderCatalog
import com.selfward.core.catalog.UnusableModels
import com.selfward.core.chat.Provider
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Asks each provider what it offers, in that provider's own way.
 *
 * OpenRouter's catalogue is public and priced, and is handled by
 * [OpenRouterCatalog]. OpenAI and Anthropic each publish a list behind their own
 * authentication, with no prices in it at all — so those are fetched here and
 * ordered by vendor tier naming instead.
 */
class HttpProviderCatalog(
    private val client: HttpClient,
    private val openRouter: OpenRouterCatalog,
    private val unusableModels: UnusableModels,
    context: Context,
    private val now: () -> Long = System::currentTimeMillis
) : ProviderCatalog {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun ranked(
        provider: Provider,
        apiKey: String?,
        force: Boolean
    ): List<ModelChoice> = when (provider) {
        Provider.OPENROUTER -> openRouterChoices(apiKey, force)
        Provider.OPENAI, Provider.ANTHROPIC -> vendorChoices(provider, apiKey, force)
    }

    private suspend fun openRouterChoices(apiKey: String?, force: Boolean): List<ModelChoice> {
        val all = runCatching { openRouter.models(apiKey, force) }.getOrDefault(emptyList())
        return ModelRanking.freeModels(all, unusableModels.all()).map { model ->
            ModelChoice(
                id = model.id,
                name = model.shortName,
                detail = listOfNotNull(
                    model.vendor.takeIf { it.isNotEmpty() },
                    model.intelligenceIndex?.let { "rated %.0f".format(it) },
                    model.contextLength.takeIf { it > 0 }?.let { "${it / 1000}k" }
                ).joinToString(" · "),
                isFree = true
            )
        }
    }

    private suspend fun vendorChoices(
        provider: Provider,
        apiKey: String?,
        force: Boolean
    ): List<ModelChoice> {
        // Both vendors put their list behind a key, so there is nothing to show
        // until one has been entered.
        if (apiKey.isNullOrBlank()) return emptyList()

        val body = if (!force && isFresh(provider)) {
            prefs.getString(bodyKey(provider), null)
        } else {
            null
        } ?: runCatching { fetch(provider, apiKey) }.getOrNull()?.also {
            prefs.edit().putString(bodyKey(provider), it).putLong(atKey(provider), now()).apply()
        } ?: prefs.getString(bodyKey(provider), null) ?: return emptyList()

        return runCatching { parse(provider, body) }.getOrDefault(emptyList())
    }

    private suspend fun fetch(provider: Provider, apiKey: String): String? {
        val response: HttpResponse = when (provider) {
            Provider.OPENAI -> client.get("https://api.openai.com/v1/models") {
                header("Authorization", "Bearer $apiKey")
            }
            Provider.ANTHROPIC -> client.get("https://api.anthropic.com/v1/models?limit=100") {
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
            }
            Provider.OPENROUTER -> return null
        }
        if (!response.status.isSuccess()) return null
        return response.bodyAsText()
    }

    private fun parse(provider: Provider, body: String): List<ModelChoice> {
        val data = json.parseToJsonElement(body).jsonObject["data"]?.jsonArray ?: return emptyList()
        val unusable = unusableModels.all()
        return data.mapNotNull { element ->
            val obj = runCatching { element.jsonObject }.getOrNull() ?: return@mapNotNull null
            val id = obj["id"]?.jsonPrimitive?.contentOrNull() ?: return@mapNotNull null
            if (!PriceTiers.isChatModel(provider, id) || id in unusable) return@mapNotNull null
            ModelChoice(
                id = id,
                name = obj["display_name"]?.jsonPrimitive?.contentOrNull() ?: id,
                detail = PriceTiers.detailFor(provider, id),
                isFree = false
            )
        }.sortedWith(compareBy({ PriceTiers.rank(provider, it.id) }, { it.id }))
    }

    private fun kotlinx.serialization.json.JsonPrimitive.contentOrNull(): String? =
        runCatching { content }.getOrNull()?.takeIf { it.isNotBlank() && it != "null" }

    private fun isFresh(provider: Provider) =
        now() - prefs.getLong(atKey(provider), 0L) < MAX_AGE_MILLIS

    private fun bodyKey(provider: Provider) = "models_${provider.name}"
    private fun atKey(provider: Provider) = "fetched_${provider.name}"

    private companion object {
        const val PREFS = "provider_catalogs"
        const val MAX_AGE_MILLIS = 24L * 60 * 60 * 1000
    }
}
