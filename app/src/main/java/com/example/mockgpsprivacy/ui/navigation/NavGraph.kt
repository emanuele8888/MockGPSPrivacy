package com.example.mockgpsprivacy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mockgpsprivacy.ui.screens.HomeScreen
import com.example.mockgpsprivacy.ui.screens.MapScreen
import com.example.mockgpsprivacy.ui.screens.ProfilesScreen
import com.example.mockgpsprivacy.ui.screens.SettingsScreen
import com.example.mockgpsprivacy.viewmodel.LocationViewModel
import com.example.mockgpsprivacy.viewmodel.ProfilesViewModel
import com.example.mockgpsprivacy.viewmodel.SettingsViewModel

/**
 * App navigation manager integrating all ViewModels.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    locationViewModel: LocationViewModel,
    profilesViewModel: ProfilesViewModel,
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = locationViewModel,
                onSaveProfile = { name, lat, lon ->
                    profilesViewModel.saveCurrentAsProfile(name, lat, lon)
                }
            )
        }
        composable(Screen.Map.route) {
            // Pass the ViewModel to MapScreen to update coordinates from the map
            MapScreen(viewModel = locationViewModel)
        }
        composable(Screen.Profiles.route) {
            ProfilesScreen(
                viewModel = profilesViewModel,
                onProfileSelected = { lat, lon ->
                    locationViewModel.updateLatitude(lat.toString())
                    locationViewModel.updateLongitude(lon.toString())
                    // Navigate back to Home after selecting a profile
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = settingsViewModel)
        }
    }
}
