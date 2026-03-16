package com.example.mockgpsprivacy.util

/**
 * Utility for input data validation.
 */
object ValidationUtils {

    /**
     * Validates if a latitude is within the correct range [-90, 90].
     */
    fun isValidLatitude(lat: Double?): Boolean {
        return lat != null && lat >= -90.0 && lat <= 90.0
    }

    /**
     * Validates if a longitude is within the correct range [-180, 180].
     */
    fun isValidLongitude(lon: Double?): Boolean {
        return lon != null && lon >= -180.0 && lon <= 180.0
    }

    /**
     * Verifies if a profile name is valid.
     */
    fun isValidProfileName(name: String): Boolean {
        return name.isNotBlank() && name.length <= 30
    }
}
