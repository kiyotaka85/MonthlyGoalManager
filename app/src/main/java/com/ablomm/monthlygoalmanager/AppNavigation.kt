package com.ablomm.monthlygoalmanager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable(
            route = "home?year={year}&month={month}",
            arguments = listOf(
                navArgument("year") {
                    type = NavType.IntType
                    defaultValue = -1 // Default value when no parameter is provided
                },
                navArgument("month") {
                    type = NavType.IntType
                    defaultValue = -1 // Default value when no parameter is provided
                }
            )
        ) { backStackEntry ->
            val goalsViewModel: GoalsViewModel = hiltViewModel()
            val year = backStackEntry.arguments?.getInt("year")
            val month = backStackEntry.arguments?.getInt("month")

            Home(
                navController = navController,
                viewModel = goalsViewModel,
                targetYear = if (year == -1) null else year,
                targetMonth = if (month == -1) null else month
            )
        }

        // Keep the original home route for compatibility
        composable("home") {
            val goalsViewModel: GoalsViewModel = hiltViewModel()
            Home(
                navController = navController,
                viewModel = goalsViewModel,
                targetYear = null,
                targetMonth = null
            )
        }

        composable(
            route = "goalDetail/{goalId}",
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goalsViewModel: GoalsViewModel = hiltViewModel()
            val goalIdString = backStackEntry.arguments?.getString("goalId")
            val goalId: UUID? = goalIdString?.let { UUID.fromString(it) }

            goalId?.let {
                GoalDetailScreen(
                    goalId = it,
                    viewModel = goalsViewModel,
                    navController = navController
                )
            }
        }

        composable(
            route = "goalEdit/{goalId}",
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goalsViewModel: GoalsViewModel = hiltViewModel()
            val goalIdString = backStackEntry.arguments?.getString("goalId")
            val goalId: UUID? = goalIdString?.let { UUID.fromString(it) }

            GoalEditForm(
                goalId = goalId,
                viewModel = goalsViewModel,
                navController = navController,
                targetMonth = null
            )
        }

        composable("edit") {
            val goalsViewModel: GoalsViewModel = hiltViewModel()
            GoalEditForm(
                goalId = null,
                viewModel = goalsViewModel,
                navController = navController,
                targetMonth = null
            )
        }

        composable(
            route = "edit?targetMonth={targetMonth}",
            arguments = listOf(navArgument("targetMonth") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val goalsViewModel: GoalsViewModel = hiltViewModel()
            val targetMonth = backStackEntry.arguments?.getInt("targetMonth") ?: 0
            GoalEditForm(
                goalId = null,
                viewModel = goalsViewModel,
                navController = navController,
                targetMonth = if (targetMonth != 0) targetMonth else null
            )
        }

        composable(
            route = "checkIn/{goalId}",
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goalsViewModel: GoalsViewModel = hiltViewModel()
            val goalIdString = backStackEntry.arguments?.getString("goalId")
            val goalId: UUID? = goalIdString?.let { UUID.fromString(it) }

            goalId?.let {
                CheckInScreen(
                    goalId = it,
                    viewModel = goalsViewModel,
                    navController = navController
                )
            }
        }

        composable(
            route = "monthlyReview/{year}/{month}",
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("month") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val goalsViewModel: GoalsViewModel = hiltViewModel()
            val year = backStackEntry.arguments?.getInt("year") ?: 2025
            val month = backStackEntry.arguments?.getInt("month") ?: 7

            MonthlyReviewWizard(
                year = year,
                month = month,
                viewModel = goalsViewModel,
                navController = navController
            )
        }

        composable(
            route = "monthlyReviewSummary/{year}/{month}",
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("month") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val goalsViewModel: GoalsViewModel = hiltViewModel()
            val year = backStackEntry.arguments?.getInt("year") ?: 2025
            val month = backStackEntry.arguments?.getInt("month") ?: 7

            MonthlyReviewSummary(
                year = year,
                month = month,
                viewModel = goalsViewModel,
                navController = navController
            )
        }



        composable("settings") {
            val goalsViewModel: GoalsViewModel = hiltViewModel()
            SettingsScreen(
                navController = navController,
                viewModel = goalsViewModel
            )
        }
    }
}
