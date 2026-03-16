package io.github.emanuele8888.mockgpsprivacy.model

/**
 * Model representing a location saved by the user with an identifying name.
 * 
 * @property id Unique identifier for the profile.
 * @property name User-defined name for the location.
 * @property point The geographic point (coordinates and optional kinematic data).
 */
data class LocationProfile(
    val id: String,
    val name: String,
    val point: GeoPoint
)
