package com.selfward.data.settings

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.selfward.core.chat.ApiConfig
import com.selfward.core.catalog.ProviderDefaults
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
        get() = apiKeyFor(provider)

    /**
     * Keys are held per provider.
     *
     * One shared key meant that switching provider kept the previous one's
     * secret on screen and, worse, in the next request: an OpenAI key was sent
     * to Anthropic simply because the selection changed and nothing cleared it.
     */
    override fun apiKeyFor(provider: Provider): String? {
        migrateLegacyKey()
        return prefs.getString(keyFor(provider), null)
    }

    override fun modelFor(provider: Provider): String =
        prefs.getString(modelKeyFor(provider), null)?.takeIf { it.isNotBlank() }
            ?: ProviderDefaults.modelFor(provider)

    /**
     * Moves a key written before keys were per-provider into the slot of
     * whichever provider was selected at the time, which is the only provider
     * it could have belonged to. Runs once; the old entry is removed.
     */
    private fun migrateLegacyKey() {
        val legacy = prefs.getString(KEY_API_KEY, null) ?: return
        val legacyModel = prefs.getString(KEY_MODEL, null)
        prefs.edit().apply {
            if (legacy.isNotBlank()) putString(keyFor(provider), legacy)
            if (!legacyModel.isNullOrBlank()) putString(modelKeyFor(provider), legacyModel)
            remove(KEY_API_KEY)
            remove(KEY_MODEL)
        }.apply()
    }

    private fun keyFor(provider: Provider) = "api_key_${provider.name}"
    private fun modelKeyFor(provider: Provider) = "model_${provider.name}"

    /**
     * Falls back per provider. A single default was handed to whichever
     * provider was selected, so choosing OpenRouter or Anthropic and saving
     * sent them an OpenAI model id and the first message failed.
     */
    override val model: String
        get() {
            migrateLegacyKey()
            return modelFor(provider)
        }

    override var useLocalModel: Boolean
        get() = prefs.getBoolean(KEY_USE_LOCAL, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_LOCAL, value).apply()

    override var localModelId: String?
        get() = prefs.getString(KEY_LOCAL_MODEL, null)
        set(value) = prefs.edit().putString(KEY_LOCAL_MODEL, value).apply()

    override var useLocalTts: Boolean
        get() = prefs.getBoolean(KEY_USE_LOCAL_TTS, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_LOCAL_TTS, value).apply()

    // Stored as a float because SharedPreferences has no double.
    override var voiceSilenceSeconds: Double
        get() = prefs.getFloat(KEY_VOICE_SILENCE, 0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_VOICE_SILENCE, value.toFloat()).apply()

    override var ttsVoice: String
        get() = prefs.getString(KEY_TTS_VOICE, null) ?: com.selfward.core.voice.VoiceCatalog.openAiVoices.first()
        set(value) = prefs.edit().putString(KEY_TTS_VOICE, value).apply()

    override var localTtsVoiceName: String?
        get() = prefs.getString(KEY_LOCAL_TTS_VOICE, null)
        set(value) = prefs.edit().putString(KEY_LOCAL_TTS_VOICE, value).apply()

    override fun save(provider: Provider, apiKey: String, model: String) {
        migrateLegacyKey()
        prefs.edit()
            .putString(KEY_PROVIDER, provider.name)
            .putString(keyFor(provider), apiKey)
            .putString(modelKeyFor(provider), model)
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
        private const val KEY_TTS_VOICE = "tts_voice"
        private const val KEY_LOCAL_TTS_VOICE = "local_tts_voice"
        private const val KEY_VOICE_SILENCE = "voice_silence_seconds"
    }
}
