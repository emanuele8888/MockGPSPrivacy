package io.github.emanuele8888.mockgpsprivacy.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Card showing the current status of the Mock GPS service.
 *
 * @param isActive Indicates if mock GPS is currently running.
 * @param modifier Layout modifier.
 */
@Composable
fun MockStatusCard(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.tertiaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.LocationOn else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isActive) 
                    MaterialTheme.colorScheme.tertiary 
                else 
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = if (isActive) "Mock GPS Active" else "Mock GPS Inactive",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isActive) 
                        MaterialTheme.colorScheme.onTertiaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isActive) 
                        "Your location is currently being simulated." 
                    else 
                        "The app is not modifying your position.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
