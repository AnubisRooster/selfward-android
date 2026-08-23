package com.theraipist.core.settings

import com.theraipist.core.chat.ApiConfig
import com.theraipist.core.chat.Provider

/**
 * Persisted provider/API-key/model choice and on-device-model preferences.
 * The real implementation ([com.theraipist.data.settings.EncryptedSecureSettings])
 * is backed by AndroidKeyStore-encrypted prefs, which isn't available under Robolectric
 * or a plain JVM test - that's exactly why this is an interface: tests substitute an
 * in-memory fake instead of exercising real OS Keystore.
 */
interface SecureSettings {
    val provider: Provider
    val apiKey: String?
    val model: String

    var useLocalModel: Boolean
    var localModelId: String?
    var useLocalTts: Boolean

    fun save(provider: Provider, apiKey: String, model: String)

    /** Built fresh from the current stored values, so callers always see the latest saved settings. */
    fun apiConfig(): ApiConfig
}
