package com.example.mockgpsprivacy.location

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import com.example.mockgpsprivacy.model.GeoPoint
import com.example.mockgpsprivacy.util.TimeUtils
import kotlin.random.Random

/**
 * Advanced GPS Manager for Bypassing Critical Applications.
 * Overwrites providers and simulates a signal with realistic physical noise.
 */
class MockLocationManager(context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    // Extended list of providers to cover Google's Fused Location Provider
    private val providers = listOf(
        LocationManager.GPS_PROVIDER, 
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
        "fused" // Essential for bypassing combined location checks
    )

    /**
     * Initializes test providers in the system.
     */
    fun setupTestProvider() {
        providers.forEach { provider ->
            try {
                if (locationManager.allProviders.contains(provider)) {
                    locationManager.removeTestProvider(provider)
                }

                locationManager.addTestProvider(
                    provider,
                    false, false, false, false, true, true, true,
                    1, // powerRequirement: POWER_LOW
                    1  // accuracy: ACCURACY_FINE
                )
                locationManager.setTestProviderEnabled(provider, true)
            } catch (e: Exception) {
                Log.e("MockGPS", "Setup error for $provider: ${e.message}")
            }
        }
    }

    /**
     * Injects location with dynamic noise to simulate a real receiver.
     * 
     * @param geoPoint The geographic point to inject.
     */
    fun pushLocation(geoPoint: GeoPoint) {
        // Atomic time synchronization
        val currentTime = TimeUtils.getCurrentTimeMillis()
        val elapsedNanos = TimeUtils.getElapsedRealtimeNanos()

        providers.forEach { provider ->
            try {
                val mockLocation = Location(provider).apply {
                    latitude = geoPoint.latitude
                    longitude = geoPoint.longitude
                    altitude = geoPoint.altitude
                    speed = geoPoint.speed
                    bearing = geoPoint.bearing
                    
                    // --- ANTI-DETECTION IMPROVEMENT: Variable Accuracy ---
                    // A real GPS signal never has a fixed accuracy (e.g., 3.5).
                    // It fluctuates between 2.0 and 8.0 meters due to atmospheric noise.
                    accuracy = 2.5f + Random.nextFloat() * 4.0f 
                    
                    time = currentTime
                    elapsedRealtimeNanos = elapsedNanos
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        verticalAccuracyMeters = 1.0f + Random.nextFloat() * 2.0f
                        bearingAccuracyDegrees = 0.5f + Random.nextFloat() * 1.5f
                        speedAccuracyMetersPerSecond = 0.1f + Random.nextFloat() * 0.5f
                    }
                }
                locationManager.setTestProviderLocation(provider, mockLocation)
            } catch (e: Exception) {
                // The 'fused' provider might not be available on some non-GMS devices
                Log.e("MockGPS", "Injection error for $provider")
            }
        }
    }

    /**
     * Removes test providers and cleans up the system state.
     */
    fun removeTestProvider() {
        providers.forEach { provider ->
            try {
                if (locationManager.allProviders.contains(provider)) {
                    locationManager.setTestProviderEnabled(provider, false)
                    locationManager.removeTestProvider(provider)
                }
            } catch (e: Exception) {
                Log.e("MockGPS", "Removal error for $provider")
            }
        }
    }
}
