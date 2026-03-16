package io.github.emanuele8888.mockgpsprivacy.model

/**
 * Representation of a geographic point with optional kinematic data.
 *
 * @property latitude The latitude of the point.
 * @property longitude The longitude of the point.
 * @property altitude The altitude of the point in meters.
 * @property speed The speed at this point in m/s.
 * @property bearing The horizontal direction of travel in degrees (0-360).
 */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val speed: Float = 0f,
    val bearing: Float = 0f
)
