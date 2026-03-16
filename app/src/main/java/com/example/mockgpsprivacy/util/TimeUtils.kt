package com.example.mockgpsprivacy.util

import android.os.SystemClock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for Mock GPS specific time management.
 * Handles synchronization between real time (Unix) and system boot time.
 */
object TimeUtils {

    private const val TIME_PATTERN = "HH:mm:ss"

    /**
     * Formats current time for UI display.
     */
    fun formatCurrentTime(): String {
        return try {
            val sdf = SimpleDateFormat(TIME_PATTERN, Locale.getDefault())
            sdf.format(Date())
        } catch (_: Exception) {
            "--:--:--"
        }
    }

    /**
     * Returns the timestamp for GPS injection (UTCMillis).
     */
    fun getCurrentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Returns system time in nanoseconds (ElapsedRealtime).
     * CRITICAL: Some apps compare 'time' and 'elapsedRealtimeNanos'.
     * If they are not synchronized, they detect mocking.
     */
    fun getElapsedRealtimeNanos(): Long {
        return SystemClock.elapsedRealtimeNanos()
    }

    /**
     * Calculates time delta for jitter speed calculation.
     * @param lastTimestamp The timestamp of the last update.
     * @return Seconds elapsed.
     */
    fun getDeltaTimeSeconds(lastTimestamp: Long): Double {
        val current = SystemClock.elapsedRealtime()
        return (current - lastTimestamp) / 1000.0
    }

    /**
     * Formats a timestamp for historical logs or profiles.
     */
    fun formatTimestamp(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (_: Exception) {
            "Unknown"
        }
    }
}
