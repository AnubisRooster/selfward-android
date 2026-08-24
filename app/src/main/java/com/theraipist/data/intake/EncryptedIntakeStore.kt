package com.theraipist.data.intake

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.theraipist.core.intake.Intake
import com.theraipist.core.intake.IntakeStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Intake answers in Keystore-backed storage. These are mental-health notes, so
 * they get the same protection as the API keys rather than plain preferences.
 */
@Singleton
class EncryptedIntakeStore @Inject constructor(
    @ApplicationContext context: Context
) : IntakeStore {
    private val appContext = context

    private val masterKey by lazy {
        MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            appContext,
            "theraipist_intake",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun load(): Intake = Intake(
        name = prefs.getString(KEY_NAME, "").orEmpty(),
        pronouns = prefs.getString(KEY_PRONOUNS, "").orEmpty(),
        age = prefs.getString(KEY_AGE, "").orEmpty(),
        concerns = prefs.getString(KEY_CONCERNS, "").orEmpty(),
        history = prefs.getString(KEY_HISTORY, "").orEmpty(),
        goals = prefs.getString(KEY_GOALS, "").orEmpty()
    )

    override fun save(intake: Intake) {
        prefs.edit()
            .putString(KEY_NAME, intake.name)
            .putString(KEY_PRONOUNS, intake.pronouns)
            .putString(KEY_AGE, intake.age)
            .putString(KEY_CONCERNS, intake.concerns)
            .putString(KEY_HISTORY, intake.history)
            .putString(KEY_GOALS, intake.goals)
            .apply()
    }

    override fun clear() {
        prefs.edit()
            .remove(KEY_NAME).remove(KEY_PRONOUNS).remove(KEY_AGE)
            .remove(KEY_CONCERNS).remove(KEY_HISTORY).remove(KEY_GOALS)
            .apply()
    }

    override var onboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDED, false)
        set(value) { prefs.edit().putBoolean(KEY_ONBOARDED, value).apply() }

    private companion object {
        const val KEY_NAME = "user_name"
        const val KEY_PRONOUNS = "user_pronouns"
        const val KEY_AGE = "user_age"
        const val KEY_CONCERNS = "intake_concerns"
        const val KEY_HISTORY = "intake_history"
        const val KEY_GOALS = "intake_goals"
        const val KEY_ONBOARDED = "onboarding_complete"
    }
}
