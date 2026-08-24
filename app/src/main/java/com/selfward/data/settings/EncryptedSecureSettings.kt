package com.selfward.data.settings

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.selfward.core.chat.ApiConfig
import com.selfward.core.chat.Provider
import com.selfward.core.settings.SecureSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedSecureSettings @Inject constructor(
    @ApplicationContext context: Context
) : SecureSettings {
    private val appContext = context

    private val masterKey by lazy {
        MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            appContext,
            "selfward_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override val provider: Provider
        get() = Provider.valueOf(prefs.getString(KEY_PROVIDER, "OPENAI") ?: "OPENAI")

    override val apiKey: String?
        get() = prefs.getString(KEY_API_KEY, null)

    override val model: String
        get() = prefs.getString(KEY_MODEL, "gpt-4o-mini") ?: "gpt-4o-mini"

    override var useLocalModel: Boolean
        get() = prefs.getBoolean(KEY_USE_LOCAL, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_LOCAL, value).apply()

    override var localModelId: String?
        get() = prefs.getString(KEY_LOCAL_MODEL, null)
        set(value) = prefs.edit().putString(KEY_LOCAL_MODEL, value).apply()

    override var useLocalTts: Boolean
        get() = prefs.getBoolean(KEY_USE_LOCAL_TTS, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_LOCAL_TTS, value).apply()

    override fun save(provider: Provider, apiKey: String, model: String) {
        prefs.edit()
            .putString(KEY_PROVIDER, provider.name)
            .putString(KEY_API_KEY, apiKey)
            .putString(KEY_MODEL, model)
            .apply()
    }

    override fun apiConfig(): ApiConfig {
        val currentProvider = provider
        val baseUrl = when (currentProvider) {
            Provider.OPENAI -> "https://api.openai.com/v1"
            Provider.OPENROUTER -> "https://openrouter.ai/api/v1"
            Provider.ANTHROPIC -> "https://api.anthropic.com/v1"
        }
        return ApiConfig(
            provider = currentProvider,
            baseUrl = baseUrl,
            apiKey = apiKey ?: "",
            model = model
        )
    }

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_PROVIDER = "provider"
        private const val KEY_MODEL = "model"
        private const val KEY_USE_LOCAL = "use_local_model"
        private const val KEY_LOCAL_MODEL = "local_model_id"
        private const val KEY_USE_LOCAL_TTS = "use_local_tts"
    }
}
