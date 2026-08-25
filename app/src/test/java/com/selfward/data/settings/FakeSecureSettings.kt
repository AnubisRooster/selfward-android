package com.selfward.data.settings

import com.selfward.core.chat.ApiConfig
import com.selfward.core.chat.Provider
import com.selfward.core.settings.SecureSettings

/** Plain in-memory [SecureSettings] for tests - no AndroidKeyStore/EncryptedSharedPreferences involved. */
class FakeSecureSettings(
    initialProvider: Provider = Provider.OPENAI,
    initialApiKey: String? = null,
    initialModel: String = "gpt-4o-mini"
) : SecureSettings {
    private var _provider = initialProvider
    private val keys = mutableMapOf<Provider, String?>(initialProvider to initialApiKey)
    private val models = mutableMapOf(initialProvider to initialModel)

    override val provider: Provider get() = _provider
    override val apiKey: String? get() = apiKeyFor(_provider)
    override val model: String get() = modelFor(_provider)

    override fun apiKeyFor(provider: Provider): String? = keys[provider]

    override fun modelFor(provider: Provider): String =
        models[provider] ?: com.selfward.core.catalog.ProviderDefaults.modelFor(provider)
    override var useLocalModel: Boolean = false
    override var localModelId: String? = null
    override var useLocalTts: Boolean = false

    override fun save(provider: Provider, apiKey: String, model: String) {
        _provider = provider
        keys[provider] = apiKey
        models[provider] = model
    }

    override fun apiConfig(): ApiConfig = ApiConfig(
        provider = _provider,
        baseUrl = "https://fake.example/v1",
        apiKey = apiKey ?: "",
        model = model
    )
}
