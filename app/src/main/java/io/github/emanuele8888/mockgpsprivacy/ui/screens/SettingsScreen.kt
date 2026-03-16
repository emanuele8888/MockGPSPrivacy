package io.github.emanuele8888.mockgpsprivacy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.emanuele8888.mockgpsprivacy.viewmodel.SettingsViewModel

/**
 * Settings screen evolved with Stealth Mode and Security Scan.
 */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Start security scan on open
    LaunchedEffect(Unit) {
        viewModel.scanSystemSecurity(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Privacy & Stealth",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: JITTER
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Physical Movement", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Enable Kinematic Jitter", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = state.jitterConfig.enabled,
                        onCheckedChange = { viewModel.toggleJitter(it) }
                    )
                }

                if (state.jitterConfig.enabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Range: ${state.jitterConfig.rangeMeters.toInt()}m",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Slider(
                        value = state.jitterConfig.rangeMeters.toFloat(),
                        onValueChange = { viewModel.updateJitterRange(it.toDouble()) },
                        valueRange = 5f..50f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 2: SECURITY MONITOR
        Text(
            text = "Anti-Detection Security",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (state.hasLogReadingApps) 
                    MaterialTheme.colorScheme.errorContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Log-Reading Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state.hasLogReadingApps) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                if (state.hasLogReadingApps) {
                    Text(
                        text = "WARNING: Detected ${state.suspiciousAppsCount} apps with 'READ_LOGS' permission. These apps could monitor system logs:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    state.suspiciousAppNames.forEach { name ->
                        Text(
                            text = "• $name",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                } else {
                    Text(
                        text = "No suspicious apps detected. Stealth mode is optimal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = { viewModel.scanSystemSecurity(context) },
                    modifier = Modifier.padding(top = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Scan Now", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // SECTION 3: PRIVACY INFO
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Pro Tip: For maximum stealth, disable the 'Allow mock locations' flag in developer settings after starting the mock (if your device allows it) or use specific root modules.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
