package com.ablomm.monthlygoalmanager.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ablomm.monthlygoalmanager.*
import com.ablomm.monthlygoalmanager.domain.enums.SortMode
import com.ablomm.monthlygoalmanager.ui.components.*
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Home Screen - Main screen for monthly goal management
 * 
 * Features:
 * - Monthly navigation
 * - Goal list with filtering and sorting
 * - Monthly review summary when completed
 * - FABs for adding goals and creating reviews
 */

/**
 * Main Home screen composable
 * 
 * @param navController Navigation controller for screen navigation
 * @param viewModel Goals view model containing app state and business logic
 */

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Home(navController: NavHostController, viewModel: GoalsViewModel) {
    // State collection with proper defaults
    val goalListState by viewModel.goalList.collectAsState(initial = emptyList())
    val isTipsHidden by viewModel.isTipsHidden.collectAsState(initial = false)
    val isHideCompletedGoals by viewModel.isHideCompletedGoals.collectAsState(initial = false)
    val higherGoals by viewModel.higherGoalList.collectAsState(initial = emptyList())
    val currentYearMonth by viewModel.currentYearMonth.collectAsState(initial = YearMonth.now())
    
    val context = LocalContext.current
    
    // Local state for sorting and menu
    var sortMode by remember { mutableStateOf(SortMode.DEFAULT) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    // Monthly review state
    val hasReviewState by viewModel.hasMonthlyReview(
        currentYearMonth.year, 
        currentYearMonth.monthValue
    ).collectAsState(initial = false)
    
    // Optimized goal filtering logic
    val filteredGoals = remember(goalListState, currentYearMonth, isHideCompletedGoals, sortMode) {
        goalListState
            .asSequence()
            .filter { goal ->
                val goalYearMonth = goal.targetMonth
                val goalYear = goalYearMonth / 1000
                val goalMonth = goalYearMonth % 1000
                currentYearMonth.year == goalYear && currentYearMonth.monthValue == goalMonth
            }
            .filter { goal ->
                if (isHideCompletedGoals) !goal.isCompleted else true
            }
            .sortedWith(
                when (sortMode) {
                    SortMode.DEFAULT -> compareBy { it.displayOrder }
                    SortMode.PRIORITY -> compareBy { 
                        when (it.priority) {
                            GoalPriority.High -> 0
                            GoalPriority.Middle -> 1
                            GoalPriority.Low -> 2
                        }
                    }
                    SortMode.PROGRESS -> compareByDescending { it.currentProgress }
                }
            )
            .toList()
    }
    
    // Formatted month/year text
    val monthYearText = remember(currentYearMonth) {
        currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
    }

    Scaffold(
        topBar = {
            HomeTopAppBar(
                monthYearText = monthYearText,
                onPreviousMonth = { viewModel.setCurrentYearMonth(currentYearMonth.minusMonths(1)) },
                onNextMonth = { viewModel.setCurrentYearMonth(currentYearMonth.plusMonths(1)) }
            )
        },
        floatingActionButton = {
            if (!hasReviewState) {
                HomeFloatingActionButtons(
                    currentYearMonth = currentYearMonth,
                    navController = navController
                )
            }
        }
    ) { innerPadding ->
        if (hasReviewState) {
            MonthlyReviewSummaryContent(
                year = currentYearMonth.year,
                month = currentYearMonth.monthValue,
                viewModel = viewModel,
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            GoalListContent(
                filteredGoals = filteredGoals,
                isTipsHidden = isTipsHidden,
                viewModel = viewModel,
                navController = navController,
                sortMode = sortMode,
                setSortMode = { sortMode = it },
                showSortMenu = showSortMenu,
                setShowSortMenu = { showSortMenu = it },
                isHideCompletedGoals = isHideCompletedGoals,
                higherGoals = higherGoals,
                monthYearText = monthYearText,
                context = context,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(
    monthYearText: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft, 
                        contentDescription = "Previous Month"
                    )
                }
                
                Text(
                    text = monthYearText,
                    style = MaterialTheme.typography.titleLarge
                )
                
                IconButton(onClick = onNextMonth) {
                    Icon(
                        Icons.Default.KeyboardArrowRight, 
                        contentDescription = "Next Month"
                    )
                }
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HomeFloatingActionButtons(
    currentYearMonth: YearMonth,
    navController: NavHostController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Monthly Review FAB
        ExtendedFloatingActionButton(
            onClick = {
                navController.navigate("monthlyReview/${currentYearMonth.year}/${currentYearMonth.monthValue}")
            },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Conduct Month End Review")
        }
        
        // Add Goal FAB  
        ExtendedFloatingActionButton(
            onClick = {
                val targetMonth = currentYearMonth.year * 1000 + currentYearMonth.monthValue
                navController.navigate("edit?targetMonth=$targetMonth")
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                Icons.Default.Add, 
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Goal")
        }
    }
}
