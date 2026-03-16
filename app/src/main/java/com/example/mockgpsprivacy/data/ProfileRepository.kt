package com.example.mockgpsprivacy.data

import android.content.Context
import android.content.SharedPreferences
import com.example.mockgpsprivacy.model.GeoPoint
import com.example.mockgpsprivacy.model.LocationProfile
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository for managing location profiles.
 */
class ProfileRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "mock_gps_profiles",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_PROFILES = "profiles_list"
    }

    /**
     * Retrieves saved profiles.
     */
    fun getProfiles(): List<LocationProfile> {
        val jsonString = prefs.getString(KEY_PROFILES, "[]") ?: "[]"
        val profiles = mutableListOf<LocationProfile>()
        
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                profiles.add(
                    LocationProfile(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        point = GeoPoint(
                            latitude = obj.getDouble("lat"),
                            longitude = obj.getDouble("lon"),
                            speed = obj.optDouble("speed", 0.0).toFloat(),
                            bearing = obj.optDouble("bearing", 0.0).toFloat()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return profiles
    }

    /**
     * Saves or updates a profile.
     */
    fun saveProfile(profile: LocationProfile) {
        val currentProfiles = getProfiles().toMutableList()
        currentProfiles.removeAll { it.id == profile.id }
        currentProfiles.add(profile)
        saveAll(currentProfiles)
    }

    /**
     * Deletes a profile.
     */
    fun deleteProfile(id: String) {
        val currentProfiles = getProfiles().toMutableList()
        currentProfiles.removeAll { it.id == id }
        saveAll(currentProfiles)
    }

    /**
     * Persists the entire profile list to SharedPreferences.
     */
    private fun saveAll(profiles: List<LocationProfile>) {
        val jsonArray = JSONArray()
        profiles.forEach { profile ->
            val obj = JSONObject().apply {
                put("id", profile.id)
                put("name", profile.name)
                put("lat", profile.point.latitude)
                put("lon", profile.point.longitude)
                put("speed", profile.point.speed.toDouble())
                put("bearing", profile.point.bearing.toDouble())
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_PROFILES, jsonArray.toString()).apply()
    }
}
