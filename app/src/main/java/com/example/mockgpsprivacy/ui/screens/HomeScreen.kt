package com.example.mockgpsprivacy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mockgpsprivacy.ui.components.CoordinateInput
import com.example.mockgpsprivacy.ui.components.MockStatusCard
import com.example.mockgpsprivacy.util.ValidationUtils
import com.example.mockgpsprivacy.viewmodel.LocationViewModel
import kotlinx.coroutines.launch

/**
 * Main screen enhanced with control dashboard and temporal log.
 */
@Composable
fun HomeScreen(
    viewModel: LocationViewModel,
    onSaveProfile: (String, Double, Double) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showSaveDialog by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf("") }

    LaunchedEffect(uiState.infoMessage, uiState.errorMessage) {
        uiState.infoMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMessages()
            }
        }
        uiState.errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMessages()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row {
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Default.Favorite, contentDescription = "Save Profile")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mock Active Status
            MockStatusCard(isActive = uiState.isMockActive)

            // Temporal injection info
            if (uiState.isMockActive) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Last update: ${uiState.lastUpdateTime}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CoordinateInput(
                latitude = uiState.latitudeText,
                longitude = uiState.longitudeText,
                onLatitudeChange = { viewModel.updateLatitude(it) },
                onLongitudeChange = { viewModel.updateLongitude(it) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Main Control
            Button(
                onClick = { viewModel.toggleMocking() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isMockActive) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = if (uiState.isMockActive) "STOP SIMULATION" else "START MOCK GPS",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Position") },
            text = {
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Profile Name") },
                    singleLine = true,
                    placeholder = { Text("e.g. Home, Work...") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (ValidationUtils.isValidProfileName(profileName)) {
                            val lat = uiState.latitudeText.toDoubleOrNull() ?: 0.0
                            val lon = uiState.longitudeText.toDoubleOrNull() ?: 0.0
                            onSaveProfile(profileName, lat, lon)
                            viewModel.showSuccessMessage("Profile saved successfully")
                            showSaveDialog = false
                            profileName = ""
                        }
                    }
                ) {
                    Text("SAVE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
