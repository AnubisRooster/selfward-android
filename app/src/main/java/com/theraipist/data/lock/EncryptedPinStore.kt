package com.theraipist.data.lock

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.theraipist.core.lock.LockoutStore
import com.theraipist.core.lock.PinStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keystore-backed storage for the PIN, the counterpart of the iOS Keychain.
 *
 * As on iOS the PIN is held as entered rather than hashed, so both apps behave
 * identically. It never leaves the device and is not backed up.
 */
@Singleton
class EncryptedPinStore @Inject constructor(
    @ApplicationContext context: Context
) : PinStore {
    private val appContext = context

    private val masterKey by lazy {
        MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            appContext,
            "theraipist_lock",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun load(): String? = prefs.getString(KEY_PIN, null)

    override fun save(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    override fun clear() {
        prefs.edit().remove(KEY_PIN).apply()
    }

    private companion object {
        const val KEY_PIN = "user_pin"
    }
}

/**
 * Lockout counters. These are deliberately *not* in the encrypted store: they
 * are not secrets, and keeping them in plain preferences means a failure to
 * open the Keystore can never leave someone permanently locked out.
 */
@Singleton
class PrefsLockoutStore @Inject constructor(
    @ApplicationContext context: Context
) : LockoutStore {
    private val prefs = context.getSharedPreferences("theraipist_lockout", Context.MODE_PRIVATE)

    override var failCount: Int
        get() = prefs.getInt(KEY_FAILS, 0)
        set(value) = prefs.edit().putInt(KEY_FAILS, value).apply()

    override var lockLevel: Int
        get() = prefs.getInt(KEY_LEVEL, 0)
        set(value) = prefs.edit().putInt(KEY_LEVEL, value).apply()

    override var lockedUntilMillis: Long
        get() = prefs.getLong(KEY_UNTIL, 0L)
        set(value) = prefs.edit().putLong(KEY_UNTIL, value).apply()

    override fun clear() {
        prefs.edit().remove(KEY_FAILS).remove(KEY_LEVEL).remove(KEY_UNTIL).apply()
    }

    private companion object {
        const val KEY_FAILS = "pin_fail_count"
        const val KEY_LEVEL = "pin_lock_level"
        const val KEY_UNTIL = "pin_lock_until"
    }
}
