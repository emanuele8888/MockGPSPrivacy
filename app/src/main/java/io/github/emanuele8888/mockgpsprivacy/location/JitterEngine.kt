package io.github.emanuele8888.mockgpsprivacy.location

import io.github.emanuele8888.mockgpsprivacy.model.GeoPoint
import kotlin.math.*
import kotlin.random.Random

/**
 * Kinematic Jitter Engine.
 * Instead of jumping randomly, it simulates physical movement with inertia,
 * friction, and an elastic force that keeps the position near the base point.
 */
class JitterEngine {

    // Jitter state (offset in meters from the base point)
    private var offsetX = 0.0
    private var offsetY = 0.0
    
    // Jitter velocity (m/s)
    private var velX = 0.0
    private var velY = 0.0

    // Physical parameters for realistic simulation
    private val damping = 0.85      // Friction to prevent infinite oscillations
    private val springK = 0.15      // Restoration force towards the center (base point)
    private val maxAcceleration = 0.4 // m/s^2 - Maximum random acceleration
    
    private var lastBasePoint: GeoPoint? = null

    /**
     * Applies a consistent kinematic jitter.
     * 
     * @param basePoint The reference geographic point.
     * @param rangeMeters Maximum allowed distance for jitter.
     * @param deltaTime Time elapsed since the last update.
     * @return A new GeoPoint with jitter applied.
     */
    fun applyJitter(basePoint: GeoPoint, rangeMeters: Double, deltaTime: Double): GeoPoint {
        // Reset state if the base point changes drastically (e.g., profile change)
        if (lastBasePoint != null && isDistanceTooLarge(lastBasePoint!!, basePoint)) {
            reset()
        }
        lastBasePoint = basePoint

        if (rangeMeters <= 0) return basePoint

        // 1. Generate random acceleration (Random Walk in Velocity Space)
        val accX = (Random.nextDouble() * 2 - 1) * maxAcceleration
        val accY = (Random.nextDouble() * 2 - 1) * maxAcceleration

        // 2. Apply physics: Elastic force towards center + Acceleration + Friction
        // F = -k*x (Hooke's Law) + F_random
        velX = (velX + (accX - springK * offsetX) * deltaTime) * damping
        velY = (velY + (accY - springK * offsetY) * deltaTime) * damping

        // 3. Update offset position
        offsetX += velX * deltaTime
        offsetY += velY * deltaTime

        // 4. Hard clamp for safety within the required range
        val currentDistance = sqrt(offsetX.pow(2) + offsetY.pow(2))
        if (currentDistance > rangeMeters) {
            val scale = rangeMeters / currentDistance
            offsetX *= scale
            offsetY *= scale
            velX *= -0.5 // Soft elastic bounce
            velY *= -0.5
        }

        // 5. Convert offset meters -> geographic coordinates
        val newLat = basePoint.latitude + (offsetY / 111111.0)
        val latRadians = Math.toRadians(basePoint.latitude)
        val newLon = basePoint.longitude + (offsetX / (111111.0 * cos(latRadians)))

        // 6. Dynamic altitude with inertia
        val altJitter = (Random.nextDouble() * 2 - 1) * 0.1 // Softer variation
        val newAlt = if (basePoint.altitude == 0.0) 24.5 + altJitter else basePoint.altitude + altJitter

        // 7. Calculate real speed and bearing of the "drift"
        // The resulting speed is a combination of base movement + jitter
        val driftSpeed = sqrt(velX.pow(2) + velY.pow(2)).toFloat()
        val bearing = (atan2(offsetX, offsetY) * 180 / PI).toFloat()

        return GeoPoint(
            latitude = newLat,
            longitude = newLon,
            altitude = newAlt,
            speed = driftSpeed,
            bearing = if (bearing < 0) bearing + 360 else bearing
        )
    }

    /**
     * Resets the jitter engine state.
     */
    private fun reset() {
        offsetX = 0.0
        offsetY = 0.0
        velX = 0.0
        velY = 0.0
    }

    /**
     * Checks if the distance between two points is too large, suggesting a major location change.
     */
    private fun isDistanceTooLarge(p1: GeoPoint, p2: GeoPoint): Boolean {
        val threshold = 0.001 // Approximately 100 meters
        return abs(p1.latitude - p2.latitude) > threshold || 
               abs(p1.longitude - p2.longitude) > threshold
    }
}
