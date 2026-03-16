package com.example.mockgpsprivacy.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mockgpsprivacy.location.MockLocationService
import com.example.mockgpsprivacy.util.TimeUtils
import com.example.mockgpsprivacy.util.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Home screen.
 */
data class LocationUiState(
    val latitudeText: String = "45.4642",
    val longitudeText: String = "9.1900",
    val isMockActive: Boolean = false,
    val isJitterEnabled: Boolean = false,
    val jitterRangeMeters: Double = 10.0,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val lastUpdateTime: String = "--:--:--",
    val isLoading: Boolean = false
)

/**
 * ViewModel for Mock GPS management.
 * Uses manual dependency injection.
 */
class LocationViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    private val context get() = getApplication<Application>().applicationContext

    fun updateLatitude(lat: String) {
        _uiState.update { it.copy(latitudeText = lat, errorMessage = null) }
    }

    fun updateLongitude(lon: String) {
        _uiState.update { it.copy(longitudeText = lon, errorMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun toggleMocking() {
        if (_uiState.value.isMockActive) {
            stopMocking()
        } else {
            startMocking()
        }
    }

    private fun startMocking() {
        val lat = _uiState.value.latitudeText.toDoubleOrNull()
        val lon = _uiState.value.longitudeText.toDoubleOrNull()

        if (!ValidationUtils.isValidLatitude(lat)) {
            _uiState.update { it.copy(errorMessage = "Invalid latitude (-90 to 90)") }
            return
        }

        if (!ValidationUtils.isValidLongitude(lon)) {
            _uiState.update { it.copy(errorMessage = "Invalid longitude (-180 to 180)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val intent = Intent(context, MockLocationService::class.java).apply {
                    action = MockLocationService.ACTION_START
                    putExtra(MockLocationService.EXTRA_LAT, lat ?: 0.0)
                    putExtra(MockLocationService.EXTRA_LON, lon ?: 0.0)
                    putExtra(MockLocationService.EXTRA_JITTER_ENABLED, _uiState.value.isJitterEnabled)
                    putExtra(MockLocationService.EXTRA_JITTER_RANGE, _uiState.value.jitterRangeMeters)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }

                _uiState.update {
                    it.copy(
                        isMockActive = true,
                        errorMessage = null,
                        lastUpdateTime = TimeUtils.formatCurrentTime(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Start error: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun stopMocking() {
        viewModelScope.launch {
            try {
                val intent = Intent(context, MockLocationService::class.java).apply {
                    action = MockLocationService.ACTION_STOP
                }
                context.stopService(intent)
                _uiState.update { it.copy(isMockActive = false, lastUpdateTime = "--:--:--") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Stop error: ${e.localizedMessage}") }
            }
        }
    }

    /**
     * Updates jitter state externally (called from MainActivity).
     */
    fun updateJitter(enabled: Boolean, range: Double) {
        _uiState.update {
            it.copy(
                isJitterEnabled = enabled,
                jitterRangeMeters = range
            )
        }
        
        // If mock is active, restart it to apply jitter in real-time
        if (_uiState.value.isMockActive) {
            startMocking()
        }
    }

    fun showSuccessMessage(message: String) {
        _uiState.update { it.copy(infoMessage = message) }
    }
}
