package com.ablomm.monthlygoalmanager

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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class SortMode {
    DEFAULT,
    PRIORITY,
    PROGRESS
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Home(navController: NavHostController, viewModel: GoalsViewModel) {
    // ...existing code...
    val goalListState = viewModel.goalList.collectAsState(initial = emptyList())
    val isTipsHidden = viewModel.isTipsHidden.collectAsState(initial = false)
    val isHideCompletedGoals = viewModel.isHideCompletedGoals.collectAsState(initial = false)
    val higherGoals = viewModel.higherGoalList.collectAsState(initial = emptyList())
    
    val context = LocalContext.current
    
    // 現在表示中の年月を管理 - ViewModelに保存して状態を保持
    val currentYearMonth by viewModel.currentYearMonth.collectAsState(initial = YearMonth.now())
    var sortMode by remember { mutableStateOf(SortMode.DEFAULT) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    // 月次レビューの存在をチェック
    val hasReviewState = viewModel.hasMonthlyReview(
        currentYearMonth.year, 
        currentYearMonth.monthValue
    ).collectAsState(initial = false)
    
    // 現在の年月に基づいてフィルタリング
    val filteredGoals = goalListState.value.filter { goal ->
        val goalYearMonth = goal.targetMonth
        val goalYear = goalYearMonth / 1000
        val goalMonth = goalYearMonth % 1000
        currentYearMonth.year == goalYear && currentYearMonth.monthValue == goalMonth
    }.let { goals ->
        // 完了済み目標の非表示機能
        if (isHideCompletedGoals.value) {
            goals.filter { !it.isCompleted }
        } else {
            goals
        }
    }.let { goals ->
        // 並べ替え機能
        when (sortMode) {
            SortMode.DEFAULT -> goals.sortedBy { it.displayOrder }
            SortMode.PRIORITY -> goals.sortedBy { 
                when (it.priority) {
                    GoalPriority.High -> 0
                    GoalPriority.Middle -> 1
                    GoalPriority.Low -> 2
                }
            }
            SortMode.PROGRESS -> goals.sortedByDescending { it.currentProgress }
        }
    }
    
    val monthYearText = currentYearMonth.format(
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous month button
                        IconButton(
                            onClick = { 
                                viewModel.setCurrentYearMonth(currentYearMonth.minusMonths(1))
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowLeft, 
                                contentDescription = "Previous Month"
                            )
                        }
                        
                        // Month and year title
                        Text(
                            text = monthYearText,
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        // Next month button
                        IconButton(
                            onClick = { 
                                viewModel.setCurrentYearMonth(currentYearMonth.plusMonths(1))
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowRight, 
                                contentDescription = "Next Month"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // レビューが完了している場合は何も表示しない
            if (!hasReviewState.value) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Create Review FAB
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
        }
    ) { innerPadding ->
        if (hasReviewState.value) {
            // レビューが完了している場合：サマリーを表示
            MonthlyReviewSummaryContent(
                year = currentYearMonth.year,
                month = currentYearMonth.monthValue,
                viewModel = viewModel,
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            // レビューが未完了の場合：目標リストを表示
            GoalListContent(
                filteredGoals = filteredGoals,
                isTipsHidden = isTipsHidden.value,
                viewModel = viewModel,
                navController = navController,
                sortMode = sortMode,
                setSortMode = { sortMode = it },
                showSortMenu = showSortMenu,
                setShowSortMenu = { showSortMenu = it },
                isHideCompletedGoals = isHideCompletedGoals.value,
                higherGoals = higherGoals.value,
                monthYearText = monthYearText,
                context = context,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}