package com.ablomm.monthlygoalmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ablomm.monthlygoalmanager.domain.enums.SortMode

/**
 * UI Helper Components - Reusable UI components for tips, controls, and empty states
 * 
 * Components:
 * - TipsCard: Helpful tips display
 * - ControlBar: Sorting and filtering controls  
 * - EmptyGoalsState: Empty state when no goals exist
 * - SwipeBackground: Background for swipe actions
 */

/**
 * Tips card for user guidance
 */
@Composable
fun TipsCard(
    onHide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💡 Tip",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Swipe left → Check-in  |  Swipe right → Edit",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888),
                    textAlign = TextAlign.Center
                )
            }
            IconButton(onClick = onHide) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Hide tips",
                    tint = Color(0xFF666666)
                )
            }
        }
    }
}

/**
 * Control bar with sorting and filtering options
 */
@Composable
fun ControlBar(
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
    sortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit,
    isHideCompletedGoals: Boolean,
    onHideCompletedGoalsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sort control
            Box {
                OutlinedButton(
                    onClick = { onShowSortMenuChange(true) }
                ) {
                    Icon(
                        Icons.Default.Sort,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        when (sortMode) {
                            SortMode.DEFAULT -> "Default"
                            SortMode.PRIORITY -> "Priority"
                            SortMode.PROGRESS -> "Progress"
                        }
                    )
                }
                
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { onShowSortMenuChange(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text("Default Order") },
                        onClick = {
                            onSortModeChange(SortMode.DEFAULT)
                            onShowSortMenuChange(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Priority") },
                        onClick = {
                            onSortModeChange(SortMode.PRIORITY)
                            onShowSortMenuChange(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Progress") },
                        onClick = {
                            onSortModeChange(SortMode.PROGRESS)
                            onShowSortMenuChange(false)
                        }
                    )
                }
            }
            
            // Hide completed toggle
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = isHideCompletedGoals,
                    onCheckedChange = onHideCompletedGoalsChange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hide Completed",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Empty state when no goals exist for the current month
 */
@Composable
fun EmptyGoalsState(
    monthYearText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎯",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No goals for $monthYearText",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to add your first goal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Background for swipe-to-dismiss actions
 */
@Composable
fun SwipeBackground(
    dismissValue: SwipeToDismissBoxValue
) {
    val backgroundColor = when (dismissValue) {
        SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50) // Green for check-in
        SwipeToDismissBoxValue.EndToStart -> Color(0xFF2196F3) // Blue for edit
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }
    
    val icon = when (dismissValue) {
        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.CheckCircle
        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Check
        SwipeToDismissBoxValue.Settled -> null
    }
    
    val alignment = when (dismissValue) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        contentAlignment = alignment
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
