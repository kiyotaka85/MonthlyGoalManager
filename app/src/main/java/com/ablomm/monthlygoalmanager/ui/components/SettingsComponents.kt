package com.ablomm.monthlygoalmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Settings UI components - reusable settings screen elements
 */

/**
 * Settings card container with consistent styling
 */
@Composable
fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Switch setting item with title and description
 */
@Composable
fun SwitchSettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Information display item with title and subtitle
 */
@Composable
fun InfoDisplayItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Action button with icon and text
 */
@Composable
fun SettingsActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

/**
 * Display settings section
 */
@Composable
fun DisplaySettingsCard(
    isTipsHidden: Boolean,
    onTipsVisibilityChanged: (Boolean) -> Unit,
    isHideCompletedGoals: Boolean,
    onHideCompletedGoalsChanged: (Boolean) -> Unit
) {
    SettingsCard(title = "🖼️ Display Settings") {
        SwitchSettingItem(
            title = "Show Tips",
            description = "Display helpful tips on home screen",
            checked = !isTipsHidden,
            onCheckedChange = { onTipsVisibilityChanged(!it) }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        SwitchSettingItem(
            title = "Show Completed Goals",
            description = "Display completed goals in goal list",
            checked = !isHideCompletedGoals,
            onCheckedChange = { onHideCompletedGoalsChanged(!it) }
        )
    }
}

/**
 * App information section
 */
@Composable
fun AppInfoCard() {
    SettingsCard(title = "ℹ️ App Information") {
        InfoDisplayItem(
            title = "Version",
            subtitle = "1.0.0"
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        InfoDisplayItem(
            title = "Monthly Goal Manager",
            subtitle = "Track and achieve your monthly goals"
        )
    }
}

/**
 * Data management section
 */
@Composable
fun DataManagementCard(
    onExportData: () -> Unit,
    onImportData: () -> Unit
) {
    SettingsCard(title = "💾 Data Management") {
        SettingsActionButton(
            icon = Icons.Default.PictureAsPdf,
            text = "Export Data",
            onClick = onExportData
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        SettingsActionButton(
            icon = Icons.Default.Add,
            text = "Import Data",
            onClick = onImportData
        )
    }
}
