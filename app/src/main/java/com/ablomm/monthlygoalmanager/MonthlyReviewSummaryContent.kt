package com.ablomm.monthlygoalmanager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

/**
 * Monthly review summary content screen
 * Optimized with component separation for better maintainability
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthlyReviewSummaryContent(
    year: Int,
    month: Int,
    viewModel: GoalsViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var monthlyReview by remember { mutableStateOf<MonthlyReview?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Collect goals state
    val allGoals by viewModel.goalList.collectAsState(initial = emptyList())
    
    // Filter goals for the specific month with optimized filtering
    val goals = remember(allGoals, year, month) {
        allGoals.filter { goal ->
            val goalYearMonth = goal.targetMonth
            val goalYear = goalYearMonth / 1000
            val goalMonth = goalYearMonth % 1000
            year == goalYear && month == goalMonth
        }
    }
    
    // Calculate statistics
    val statistics = remember(goals) {
        GoalStatistics(
            completedGoals = goals.count { it.isCompleted },
            totalGoals = goals.size,
            averageProgress = if (goals.isNotEmpty()) goals.map { it.currentProgress }.average() else 0.0
        )
    }
    
    // データロード
    LaunchedEffect(year, month) {
        monthlyReview = viewModel.getMonthlyReview(year, month)
        isLoading = false
    }
    
    if (isLoading) {
        LoadingIndicator(modifier = modifier)
    } else {
        MonthlyReviewContent(
            modifier = modifier,
            monthlyReview = monthlyReview,
            goals = goals,
            statistics = statistics,
            year = year,
            month = month,
            navController = navController
        )
    }
}

/**
 * Data class for goal statistics
 */
private data class GoalStatistics(
    val completedGoals: Int,
    val totalGoals: Int,
    val averageProgress: Double
)

/**
 * Loading indicator composable
 */
@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Main monthly review content
 */
@Composable
private fun MonthlyReviewContent(
    modifier: Modifier,
    monthlyReview: MonthlyReview?,
    goals: List<Goal>,
    statistics: GoalStatistics,
    year: Int,
    month: Int,
    navController: NavHostController
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            MonthlyReviewHeader()
        }
        
        item {
            GoalsSummaryCard(
                completedGoals = statistics.completedGoals,
                totalGoals = statistics.totalGoals,
                averageProgress = statistics.averageProgress
            )
        }
        
        monthlyReview?.let { review ->
            item {
                MonthlyReflectionCard(
                    reflection = review.overallReflection
                )
            }
        }
        
        item {
            GoalsOverviewCard(goals = goals)
        }
        
        item {
            MonthlyReviewActionsCard(
                onEditClick = {
                    navController.navigate("monthlyReview/$year/$month")
                },
                onDeleteClick = {
                    // TODO: 削除機能を実装
                }
            )
        }
    }
}
