package com.example.mockgpsprivacy.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mockgpsprivacy.data.PreferencesManager
import com.example.mockgpsprivacy.model.JitterConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for settings, including security controls.
 */
data class SettingsUiState(
    val jitterConfig: JitterConfig,
    val suspiciousAppsCount: Int = 0,
    val suspiciousAppNames: List<String> = emptyList(),
    val hasLogReadingApps: Boolean = false,
    val isDeveloperModeActive: Boolean = false
)

/**
 * ViewModel for managing privacy and jitter settings.
 */
class SettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(jitterConfig = preferencesManager.getJitterConfig()))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /**
     * Exposes jitter config for compatibility with existing code.
     */
    val jitterConfig: StateFlow<JitterConfig> = MutableStateFlow(preferencesManager.getJitterConfig()).apply {
        viewModelScope.launch {
            _uiState.collect { value = it.jitterConfig }
        }
    }

    /**
     * Performs a system scan for apps that could compromise stealth mode.
     */
    fun scanSystemSecurity(context: Context) {
        viewModelScope.launch {
            val pm = context.packageManager
            val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            
            val logReaderNames = mutableListOf<String>()
            
            for (pkg in packages) {
                // Null-safe check for applicationInfo
                val appInfo = pkg.applicationInfo ?: continue

                // Exclude system apps to reduce false positives
                val isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                if (isSystemApp) continue

                val permissions = pkg.requestedPermissions
                if (permissions != null) {
                    if (permissions.contains("android.permission.READ_LOGS")) {
                        val label = appInfo.loadLabel(pm).toString()
                        logReaderNames.add(label)
                    }
                }
            }

            _uiState.update { it.copy(
                suspiciousAppsCount = logReaderNames.size,
                suspiciousAppNames = logReaderNames,
                hasLogReadingApps = logReaderNames.isNotEmpty()
            ) }
        }
    }

    /**
     * Toggles the jitter simulation on or off.
     */
    fun toggleJitter(enabled: Boolean) {
        viewModelScope.launch {
            val newConfig = _uiState.value.jitterConfig.copy(enabled = enabled)
            _uiState.update { it.copy(jitterConfig = newConfig) }
            preferencesManager.saveJitterConfig(newConfig)
        }
    }

    /**
     * Updates the jitter radius range.
     */
    fun updateJitterRange(range: Double) {
        viewModelScope.launch {
            val newConfig = _uiState.value.jitterConfig.copy(rangeMeters = range)
            _uiState.update { it.copy(jitterConfig = newConfig) }
            preferencesManager.saveJitterConfig(newConfig)
        }
    }
}
