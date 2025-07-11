package com.ablomm.monthlygoalmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ablomm.monthlygoalmanager.*
import com.ablomm.monthlygoalmanager.domain.enums.SortMode

/**
 * Goal list components - optimized with component separation
 */

/**
 * Main goal list content with filtering and sorting
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
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Tips card (if not hidden)
        if (!isTipsHidden) {
            item {
                TipsCard(
                    onHide = { viewModel.setTipsHidden(true) }
                )
            }
        }
        
        // Control bar with filters and sort
        item {
            ControlBar(
                showSortMenu = showSortMenu,
                onShowSortMenuChange = setShowSortMenu,
                sortMode = sortMode,
                onSortModeChange = setSortMode,
                isHideCompletedGoals = isHideCompletedGoals,
                onHideCompletedGoalsChange = viewModel::setHideCompletedGoals
            )
        }
        
        // Goal list
        if (filteredGoals.isEmpty()) {
            item {
                EmptyGoalsState(monthYearText = monthYearText)
            }
        } else {
            items(filteredGoals, key = { it.id }) { goalItem ->
                GoalCardWithSwipe(
                    goalItem = goalItem,
                    navController = navController
                )
            }
        }
    }
}

// ... rest of the components from GoalListComponents.kt
