package com.example.mockgpsprivacy.data

import android.content.Context
import android.content.SharedPreferences
import com.example.mockgpsprivacy.model.JitterConfig

/**
 * Manager for local persistence of settings using SharedPreferences.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "mock_gps_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_JITTER_ENABLED = "jitter_enabled"
        private const val KEY_JITTER_RANGE = "jitter_range"
    }

    /**
     * Saves the Jitter configuration.
     */
    fun saveJitterConfig(config: JitterConfig) {
        prefs.edit().apply {
            putBoolean(KEY_JITTER_ENABLED, config.enabled)
            putFloat(KEY_JITTER_RANGE, config.rangeMeters.toFloat())
            apply()
        }
    }

    /**
     * Retrieves the Jitter configuration.
     */
    fun getJitterConfig(): JitterConfig {
        return JitterConfig(
            enabled = prefs.getBoolean(KEY_JITTER_ENABLED, false),
            rangeMeters = prefs.getFloat(KEY_JITTER_RANGE, 10.0f).toDouble()
        )
    }
}
