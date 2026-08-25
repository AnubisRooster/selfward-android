package com.selfward.data.catalog

import android.content.Context
import com.selfward.core.catalog.UnusableModels

/**
 * Plain prefs, not the encrypted store: this is a list of model names that
 * turned us away, which is neither secret nor about the client.
 */
class PrefsUnusableModels(context: Context) : UnusableModels {

    private val prefs = context.getSharedPreferences("openrouter_unusable", Context.MODE_PRIVATE)

    override fun all(): Set<String> = prefs.getStringSet(KEY, emptySet())?.toSet() ?: emptySet()

    override fun remember(modelId: String, reason: String) {
        prefs.edit()
            .putStringSet(KEY, all() + modelId)
            .putString("reason_$modelId", reason.take(REASON_LIMIT))
            .apply()
    }

    override fun forget(modelId: String) {
        prefs.edit().putStringSet(KEY, all() - modelId).remove("reason_$modelId").apply()
    }

    override fun reasonFor(modelId: String): String? = prefs.getString("reason_$modelId", null)

    override fun working(): Set<String> =
        prefs.getStringSet(WORKING_KEY, emptySet())?.toSet() ?: emptySet()

    override fun rememberWorking(modelId: String) {
        prefs.edit()
            .putStringSet(WORKING_KEY, working() + modelId)
            .putStringSet(KEY, all() - modelId)
            .remove("reason_$modelId")
            .apply()
    }

    override fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val KEY = "unusable_model_ids"
        const val WORKING_KEY = "working_model_ids"
        /** Enough to explain the exclusion in a log or a support question. */
        const val REASON_LIMIT = 300
    }
}
