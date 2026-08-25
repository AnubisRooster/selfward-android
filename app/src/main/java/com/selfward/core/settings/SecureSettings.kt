package com.selfward.core.settings

import com.selfward.core.chat.ApiConfig
import com.selfward.core.chat.Provider

/**
 * Persisted provider/API-key/model choice and on-device-model preferences.
 * The real implementation ([com.selfward.data.settings.EncryptedSecureSettings])
 * is backed by AndroidKeyStore-encrypted prefs, which isn't available under Robolectric
 * or a plain JVM test - that's exactly why this is an interface: tests substitute an
 * in-memory fake instead of exercising real OS Keystore.
 */
interface SecureSettings {
    val provider: Provider

    /** The key for the provider currently selected. */
    val apiKey: String?

    /** The model for the provider currently selected. */
    val model: String

    /**
     * The key held for [provider], whichever one is selected right now.
     *
     * Keys are stored per provider because they are not interchangeable: a
     * single shared key meant that selecting a different provider carried the
     * previous one's secret along and sent it to whoever was chosen next.
     */
    fun apiKeyFor(provider: Provider): String?

    /** The model held for [provider], or that provider's default. */
    fun modelFor(provider: Provider): String

    var useLocalModel: Boolean
    var localModelId: String?
    var useLocalTts: Boolean

    /**
     * Seconds of quiet that end a spoken turn in voice mode. Zero means unset,
     * and is read as the default rather than as "end every turn instantly".
     */
    var voiceSilenceSeconds: Double

    fun save(provider: Provider, apiKey: String, model: String)

    /** Built fresh from the current stored values, so callers always see the latest saved settings. */
    fun apiConfig(): ApiConfig
}
