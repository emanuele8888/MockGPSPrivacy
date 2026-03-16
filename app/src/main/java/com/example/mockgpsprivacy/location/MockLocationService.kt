package com.example.mockgpsprivacy.location

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.mockgpsprivacy.model.GeoPoint
import com.example.mockgpsprivacy.model.JitterConfig
import com.example.mockgpsprivacy.util.NotificationHelper
import com.example.mockgpsprivacy.util.TimeUtils

/**
 * Advanced Foreground Service for GPS simulation.
 * Integrates a WakeLock to ensure constant execution in the background and with the screen off.
 */
class MockLocationService : LifecycleService() {

    private var locationController: LocationController? = null
    private lateinit var mockLocationManager: MockLocationManager
    private lateinit var jitterEngine: JitterEngine
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val ACTION_START = "START_MOCKING"
        const val ACTION_STOP = "STOP_MOCKING"
        const val EXTRA_LAT = "EXTRA_LAT"
        const val EXTRA_LON = "EXTRA_LON"
        const val EXTRA_JITTER_ENABLED = "EXTRA_JITTER_ENABLED"
        const val EXTRA_JITTER_RANGE = "EXTRA_JITTER_RANGE"
    }

    override fun onCreate() {
        super.onCreate()
        try {
            mockLocationManager = MockLocationManager(this)
            jitterEngine = JitterEngine()
            NotificationHelper.createNotificationChannel(this)
            
            // WakeLock initialization to prevent CPU suspension
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MockGPS:WakeLock")
            
            Log.d("MockLocationService", "Service created with WakeLock ready")
        } catch (e: Exception) {
            Log.e("MockLocationService", "Error creating the service", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        when (intent?.action) {
            ACTION_START -> {
                try {
                    val lat = intent.getDoubleExtra(EXTRA_LAT, 0.0)
                    val lon = intent.getDoubleExtra(EXTRA_LON, 0.0)
                    val jitterEnabled = intent.getBooleanExtra(EXTRA_JITTER_ENABLED, false)
                    val jitterRange = intent.getDoubleExtra(EXTRA_JITTER_RANGE, 10.0)

                    // WakeLock acquisition: CPU will remain active
                    if (wakeLock?.isHeld == false) {
                        wakeLock?.acquire(10*60*1000L /* Safety timeout */)
                    }

                    val lastUpdate = TimeUtils.formatCurrentTime()
                    startForeground(
                        NotificationHelper.NOTIFICATION_ID,
                        NotificationHelper.buildNotification(
                            this, 
                            "Mock active at $lat, $lon (Updated: $lastUpdate)"
                        )
                    )

                    if (locationController == null) {
                        locationController = LocationController(
                            mockLocationManager, 
                            jitterEngine,
                            lifecycleScope
                        )
                    }
                    
                    locationController?.startMocking(
                        GeoPoint(lat, lon),
                        JitterConfig(jitterEnabled, jitterRange)
                    )
                    
                    Log.d("MockLocationService", "Mock GPS started with WakeLock active")
                } catch (e: Exception) {
                    Log.e("MockLocationService", "Error starting mock", e)
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stopMockingAndCleanup()
            }
        }
        // START_STICKY tells the system to restart the service if it's terminated due to memory pressure
        return START_STICKY
    }

    /**
     * Stops mocking and releases resources.
     */
    private fun stopMockingAndCleanup() {
        try {
            locationController?.stopMocking()
            locationController = null
            
            // WakeLock release
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()
            Log.d("MockLocationService", "Mock GPS stopped and WakeLock released")
        } catch (e: Exception) {
            Log.e("MockLocationService", "Error stopping mock", e)
        }
    }

    override fun onDestroy() {
        stopMockingAndCleanup()
        super.onDestroy()
    }
}
