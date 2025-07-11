/**
 * UI Helper Components - Reusable UI components for tips, controls, and empty states
 * 
 * Components:
 * - TipsCard: Helpful tips display
 * - ControlBar: Sorting and filtering controls  
 * - EmptyGoalsState: Empty state when no goals exist
 * - SwipeBackground: Background for swipe actions
 */

package com.ablomm.monthlygoalmanager

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tips card for user guidance
 */
@Composable
fun TipsCard(
    onHideTips: () -> Unit,
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
            IconButton(onClick = onHideTips) {
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
    isHideCompletedGoals: Boolean,
    onToggleHideCompleted: () -> Unit,
    onExportPdf: () -> Unit,
    sortMode: SortMode,
    showSortMenu: Boolean,
    onSortModeChange: (SortMode) -> Unit,
    onShowSortMenuChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hide completed goals toggle
            IconButton(onClick = onToggleHideCompleted) {
                Icon(
                    imageVector = if (isHideCompletedGoals) Icons.Default.CheckCircle else Icons.Default.Check,
                    contentDescription = if (isHideCompletedGoals) "Show completed goals" else "Hide completed goals",
                    tint = if (isHideCompletedGoals) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            
            // PDF export button
            IconButton(onClick = onExportPdf) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "Export to PDF"
                )
            }
            
            // Sort menu
            Box {
                IconButton(onClick = { onShowSortMenuChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Sort goals"
                    )
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { onShowSortMenuChange(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text("Default order") },
                        onClick = {
                            onSortModeChange(SortMode.DEFAULT)
                            onShowSortMenuChange(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Priority (High → Low)") },
                        onClick = {
                            onSortModeChange(SortMode.PRIORITY)
                            onShowSortMenuChange(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Progress (High → Low)") },
                        onClick = {
                            onSortModeChange(SortMode.PROGRESS)
                            onShowSortMenuChange(false)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Empty state when no goals exist
 */
@Composable
fun EmptyGoalsState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎯",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No goals for this month",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to add a new goal",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Background content for swipe actions
 */
@Composable
fun SwipeBackground(
    dismissValue: SwipeToDismissBoxValue,
    modifier: Modifier = Modifier
) {
    val color = when (dismissValue) {
        SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50).copy(alpha = 0.8f)
        SwipeToDismissBoxValue.EndToStart -> Color(0xFF2196F3).copy(alpha = 0.8f)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(color, shape = RoundedCornerShape(6.dp)),
        contentAlignment = when (dismissValue) {
            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        when (dismissValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text("📊", fontSize = 24.sp)
                    Text("Check-in", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            SwipeToDismissBoxValue.EndToStart -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text("Edit", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("✏️", fontSize = 24.sp)
                }
            }
            else -> {}
        }
    }
}
