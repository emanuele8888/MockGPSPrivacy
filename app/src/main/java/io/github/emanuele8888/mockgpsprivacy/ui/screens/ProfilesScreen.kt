package io.github.emanuele8888.mockgpsprivacy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.emanuele8888.mockgpsprivacy.util.ValidationUtils
import io.github.emanuele8888.mockgpsprivacy.viewmodel.ProfilesViewModel
import kotlinx.coroutines.launch

/**
 * Location profiles management screen.
 * Integrated with error and loading state management.
 */
@Composable
fun ProfilesScreen(
    viewModel: ProfilesViewModel,
    onProfileSelected: (Double, Double) -> Unit
) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var profileToDeleteId by remember { mutableStateOf<String?>(null) }
    var profileToRename by remember { mutableStateOf<Pair<String, String>?>(null) } // ID to Name
    var newNameText by remember { mutableStateOf("") }

    // Show error if present
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Saved Profiles",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                if (profiles.isEmpty() && !isLoading) {
                    Box(modifier = Modifier.fillWeight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No saved profiles.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(profiles) { profile ->
                            ProfileItem(
                                name = profile.name,
                                latitude = profile.point.latitude,
                                longitude = profile.point.longitude,
                                onSelect = { onProfileSelected(profile.point.latitude, profile.point.longitude) },
                                onDelete = { profileToDeleteId = profile.id },
                                onRename = { 
                                    profileToRename = profile.id to profile.name
                                    newNameText = profile.name
                                }
                            )
                        }
                    }
                }
            }

            // Loading overlay
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (profileToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { profileToDeleteId = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this profile? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        profileToDeleteId?.let { viewModel.deleteProfile(it) }
                        profileToDeleteId = null
                    }
                ) {
                    Text("DELETE", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDeleteId = null }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Rename Dialog
    if (profileToRename != null) {
        AlertDialog(
            onDismissRequest = { profileToRename = null },
            title = { Text("Rename Profile") },
            text = {
                OutlinedTextField(
                    value = newNameText,
                    onValueChange = { newNameText = it },
                    label = { Text("New Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (ValidationUtils.isValidProfileName(newNameText)) {
                            profileToRename?.let { viewModel.renameProfile(it.first, newNameText) }
                            profileToRename = null
                        }
                    }
                ) {
                    Text("SAVE")
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToRename = null }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun ProfileItem(
    name: String,
    latitude: Double,
    longitude: Double,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Lat: $latitude, Lon: $longitude",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRename) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Rename",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Modifier to center content when list is empty.
 */
private fun Modifier.fillWeight(weight: Float): Modifier = this.then(Modifier.fillMaxHeight().fillMaxWidth())
