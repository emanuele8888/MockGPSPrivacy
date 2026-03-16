package io.github.emanuele8888.mockgpsprivacy.location

import android.os.SystemClock
import android.util.Log
import io.github.emanuele8888.mockgpsprivacy.model.GeoPoint
import io.github.emanuele8888.mockgpsprivacy.model.JitterConfig
import io.github.emanuele8888.mockgpsprivacy.util.TimeUtils
import kotlinx.coroutines.*

/**
 * Controller for real GPS simulation.
 * Manages the continuous injection of coordinates into the Android system.
 * Accepts an external CoroutineScope for better lifecycle management.
 */
class LocationController(
    private val mockLocationManager: MockLocationManager,
    private val jitterEngine: JitterEngine,
    private val scope: CoroutineScope
) {
    private var mockJob: Job? = null
    private var lastUpdateTimestamp: Long = 0

    private var currentConfig = JitterConfig(enabled = false, rangeMeters = 10.0)

    /**
     * Starts the REAL GPS location change on the device.
     */
    fun startMocking(basePoint: GeoPoint, config: JitterConfig) {
        stopMocking()
        currentConfig = config
        lastUpdateTimestamp = SystemClock.elapsedRealtime()

        mockJob = scope.launch {
            try {
                // Initialize the test provider in the system
                mockLocationManager.setupTestProvider()

                while (isActive) {
                    val deltaTime = TimeUtils.getDeltaTimeSeconds(lastUpdateTimestamp)
                    lastUpdateTimestamp = SystemClock.elapsedRealtime()

                    // Calculate the point (base or with jitter)
                    val targetPoint = if (currentConfig.enabled) {
                        jitterEngine.applyJitter(basePoint, currentConfig.rangeMeters, deltaTime)
                    } else {
                        basePoint
                    }

                    // REAL INJECTION: Send the coordinate to the Android system
                    mockLocationManager.pushLocation(targetPoint)

                    Log.d("MockGPS", "Injected position: ${targetPoint.latitude}, ${targetPoint.longitude}")

                    // 1-second interval to keep the signal stable
                    delay(1000L)
                }
            } catch (e: CancellationException) {
                Log.d("LocationController", "Mocking cancelled")
                throw e
            } catch (e: Exception) {
                Log.e("LocationController", "Critical error during mocking: ${e.message}", e)
            } finally {
                mockLocationManager.removeTestProvider()
            }
        }
    }

    /**
     * Stops mocking and cleans up resources.
     */
    fun stopMocking() {
        mockJob?.cancel()
        mockJob = null
        mockLocationManager.removeTestProvider()
    }

    /**
     * Updates the jitter configuration.
     */
    fun updateConfig(config: JitterConfig) {
        currentConfig = config
    }
}
