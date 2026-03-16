package com.example.mockgpsprivacy

import android.Manifest
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mockgpsprivacy.data.PreferencesManager
import com.example.mockgpsprivacy.data.ProfileRepository
import com.example.mockgpsprivacy.ui.navigation.NavGraph
import com.example.mockgpsprivacy.ui.navigation.bottomNavItems
import com.example.mockgpsprivacy.ui.theme.MockGPSPrivacyTheme
import com.example.mockgpsprivacy.util.PermissionUtils
import com.example.mockgpsprivacy.viewmodel.LocationViewModel
import com.example.mockgpsprivacy.viewmodel.ProfilesViewModel
import com.example.mockgpsprivacy.viewmodel.SettingsViewModel

/**
 * Main Activity handling navigation and manual dependency injection.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Clean initialization of persistence components
        val preferencesManager = PreferencesManager(applicationContext)
        val profileRepository = ProfileRepository(applicationContext)

        enableEdgeToEdge()
        
        setContent {
            MockGPSPrivacyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MockGPSPrivacyApp(
                        preferencesManager = preferencesManager,
                        profileRepository = profileRepository
                    )
                }
            }
        }
    }
}

/**
 * Composable representing the root of the application's UI and ViewModel management.
 */
@Composable
fun MockGPSPrivacyApp(
    preferencesManager: PreferencesManager,
    profileRepository: ProfileRepository
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Manual factories to inject dependencies into ViewModels without Hilt
    val locationViewModel: LocationViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LocationViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(preferencesManager) as T
            }
        }
    )

    val profilesViewModel: ProfilesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfilesViewModel(profileRepository) as T
            }
        }
    )

    // Jitter synchronization
    val jitterConfig by settingsViewModel.jitterConfig.collectAsState()
    LaunchedEffect(jitterConfig) {
        locationViewModel.updateJitter(jitterConfig.enabled, jitterConfig.rangeMeters)
    }

    // Permissions management
    var hasPermission by remember { mutableStateOf(PermissionUtils.hasLocationPermissions(context)) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasPermission = isGranted }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavGraph(
                navController = navController,
                locationViewModel = locationViewModel,
                profilesViewModel = profilesViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }
}
