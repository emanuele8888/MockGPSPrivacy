package com.example.mockgpsprivacy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Definition of the application's navigation routes.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    /**
     * Home screen route.
     */
    object Home : Screen("home", "Home", Icons.Default.Home)
    
    /**
     * Map screen route.
     */
    object Map : Screen("map", "Map", Icons.Default.LocationOn)
    
    /**
     * Profiles screen route.
     */
    object Profiles : Screen("profiles", "Profiles", Icons.AutoMirrored.Filled.List)
    
    /**
     * Settings screen route.
     */
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

/**
 * List of items displayed in the bottom navigation bar.
 */
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Map,
    Screen.Profiles,
    Screen.Settings
)
