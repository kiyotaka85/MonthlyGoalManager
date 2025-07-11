/**
 * Goal List Components - UI components for displaying and managing goal lists
 * 
 * Components:
 * - GoalCard: Individual goal display card with click actions
 * - GoalTextArea: Text display area for goal title and description
 * - GoalStatusArea: Status emoji and progress display
 * - GoalListContent: Main list container with filtering, sorting, and tips
 * - TipsCard: Helpful tips display for user guidance
 * - ControlBar: Sorting and filtering controls
 */

package com.ablomm.monthlygoalmanager

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.NavHostController

/**
 * Individual goal card component
 * 
 * @param goalItem The goal data to display
 * @param navController Navigation controller for actions
 * @param modifier Optional modifier for styling
 */

@Composable
fun GoalCard(
    goalItem: GoalItem,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoalTextArea(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navController.navigate("edit/${goalItem.id}") },
                    title = goalItem.title,
                    description = goalItem.detailedDescription
                )
                GoalStatusArea(
                    modifier = Modifier
                        .width(64.dp)
                        .clickable { navController.navigate("checkin/${goalItem.id}") },
                    statusEmoji = when (goalItem.currentProgress) {
                        0 -> "🆕"
                        100 -> "✅"
                        else -> "⏳"
                    },
                    progress = goalItem.currentProgress
                )
            }
        }
    }
}

@Composable
fun GoalTextArea(
    modifier: Modifier = Modifier,
    title: String,
    description: String?
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold
        )
        if (!description.isNullOrBlank()) {
            Text(
                text = description,
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            )
        }
    }
}

@Composable
fun GoalStatusArea(
    modifier: Modifier = Modifier,
    statusEmoji: String = "🆕",
    progress: Int = 0
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = statusEmoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "$progress %",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Main goal list content with optimized structure
 */
@Composable
fun GoalListContent(
    filteredGoals: List<GoalItem>,
    isTipsHidden: Boolean,
    viewModel: GoalsViewModel,
    navController: NavHostController,
    sortMode: SortMode,
    setSortMode: (SortMode) -> Unit,
    showSortMenu: Boolean,
    setShowSortMenu: (Boolean) -> Unit,
    isHideCompletedGoals: Boolean,
    higherGoals: List<HigherGoal>,
    monthYearText: String,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    if (filteredGoals.isEmpty()) {
        EmptyGoalsState(modifier = modifier)
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            // Tips section
            if (!isTipsHidden) {
                item {
                    TipsCard(onHideTips = { viewModel.setTipsHidden(true) })
                }
            }
            
            // Control bar
            item {
                ControlBar(
                    isHideCompletedGoals = isHideCompletedGoals,
                    onToggleHideCompleted = { viewModel.setHideCompletedGoals(!isHideCompletedGoals) },
                    onExportPdf = {
                        val pdfExporter = PdfExporter(context)
                        val intent = pdfExporter.exportGoalsToPdf(
                            goals = filteredGoals,
                            higherGoals = higherGoals,
                            yearMonth = monthYearText
                        )
                        intent?.let {
                            context.startActivity(Intent.createChooser(it, "Share Goals PDF"))
                        }
                    },
                    sortMode = sortMode,
                    showSortMenu = showSortMenu,
                    onSortModeChange = setSortMode,
                    onShowSortMenuChange = setShowSortMenu
                )
            }
            
            // Goal cards with swipe actions
            items(
                items = filteredGoals,
                key = { it.id }
            ) { goalItem ->
                GoalCardWithSwipe(
                    goalItem = goalItem,
                    navController = navController
                )
            }
        }
    }
}

/**
 * Goal card with swipe-to-dismiss functionality
 */
@Composable
private fun GoalCardWithSwipe(
    goalItem: GoalItem,
    navController: NavHostController
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.25f },
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    navController.navigate("checkin/${goalItem.id}")
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    navController.navigate("edit/${goalItem.id}")
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            SwipeBackground(dismissValue = dismissState.targetValue)
        }
    ) {
        GoalCard(goalItem = goalItem, navController = navController)
    }
}
