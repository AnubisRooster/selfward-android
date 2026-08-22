package com.theraipist.data.settings

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.theraipist.core.chat.Provider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureSettings @Inject constructor(
    @ApplicationContext context: Context
) {
    private val appContext = context

    private val masterKey by lazy {
        MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            appContext,
            "theraipist_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    val provider: Provider
        get() = Provider.valueOf(prefs.getString(KEY_PROVIDER, "OPENAI") ?: "OPENAI")

    val apiKey: String?
        get() = prefs.getString(KEY_API_KEY, null)

    val model: String
        get() = prefs.getString(KEY_MODEL, "gpt-4o-mini") ?: "gpt-4o-mini"

    var useLocalModel: Boolean
        get() = prefs.getBoolean(KEY_USE_LOCAL, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_LOCAL, value).apply()

    var localModelId: String?
        get() = prefs.getString(KEY_LOCAL_MODEL, null)
        set(value) = prefs.edit().putString(KEY_LOCAL_MODEL, value).apply()

    fun save(provider: Provider, apiKey: String, model: String) {
        prefs.edit()
            .putString(KEY_PROVIDER, provider.name)
            .putString(KEY_API_KEY, apiKey)
            .putString(KEY_MODEL, model)
            .apply()
    }

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_PROVIDER = "provider"
        private const val KEY_MODEL = "model"
        private const val KEY_USE_LOCAL = "use_local_model"
        private const val KEY_LOCAL_MODEL = "local_model_id"
    }
}
