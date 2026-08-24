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
    private var _apiKey = initialApiKey
    private var _model = initialModel

    override val provider: Provider get() = _provider
    override val apiKey: String? get() = _apiKey
    override val model: String get() = _model
    override var useLocalModel: Boolean = false
    override var localModelId: String? = null
    override var useLocalTts: Boolean = false

    override fun save(provider: Provider, apiKey: String, model: String) {
        _provider = provider
        _apiKey = apiKey
        _model = model
    }

    override fun apiConfig(): ApiConfig = ApiConfig(
        provider = _provider,
        baseUrl = "https://fake.example/v1",
        apiKey = _apiKey ?: "",
        model = _model
    )
}
