package com.example.mockgpsprivacy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mockgpsprivacy.data.ProfileRepository
import com.example.mockgpsprivacy.model.GeoPoint
import com.example.mockgpsprivacy.model.LocationProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * ViewModel for managing the list of saved location profiles.
 * Uses manual dependency injection for maximum privacy.
 */
class ProfilesViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _profiles = MutableStateFlow<List<LocationProfile>>(emptyList())
    val profiles: StateFlow<List<LocationProfile>> = _profiles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadProfiles()
    }

    /**
     * Asynchronously loads profiles from the local repository.
     */
    fun loadProfiles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profilesList = withContext(Dispatchers.IO) {
                    repository.getProfiles()
                }
                _profiles.update { profilesList }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Saves a new location to the profiles.
     */
    fun saveCurrentAsProfile(name: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                // Creating GeoPoint with all necessary parameters for consistency
                val point = GeoPoint(
                    latitude = lat,
                    longitude = lon,
                    speed = 0f,
                    bearing = 0f
                )
                
                val newProfile = LocationProfile(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    point = point
                )
                
                withContext(Dispatchers.IO) {
                    repository.saveProfile(newProfile)
                }
                loadProfiles()
            } catch (e: Exception) {
                _errorMessage.value = "Save error: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Updates the name of an existing profile.
     */
    fun renameProfile(id: String, newName: String) {
        viewModelScope.launch {
            try {
                val currentProfiles = _profiles.value
                val profileToUpdate = currentProfiles.find { it.id == id }
                
                if (profileToUpdate != null) {
                    val updatedProfile = profileToUpdate.copy(name = newName)
                    withContext(Dispatchers.IO) {
                        repository.saveProfile(updatedProfile)
                    }
                    loadProfiles()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Rename error: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Deletes an existing profile.
     */
    fun deleteProfile(id: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteProfile(id)
                }
                loadProfiles()
            } catch (e: Exception) {
                _errorMessage.value = "Delete error: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
