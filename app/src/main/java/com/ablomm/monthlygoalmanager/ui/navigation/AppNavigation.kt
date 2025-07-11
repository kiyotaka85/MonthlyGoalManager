package com.ablomm.monthlygoalmanager.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.ablomm.monthlygoalmanager.*
import com.ablomm.monthlygoalmanager.ui.screens.*
import java.util.UUID

/**
 * Main navigation configuration for the app
 * 
 * Features:
 * - Bottom navigation for main tabs (Home, Higher Goals, Settings)
 * - Nested navigation for detailed screens
 * - Proper ViewModel scope management with Hilt
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    // Get current route for bottom bar visibility
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Routes that should show bottom navigation
    val tabRoutes = listOf("home", "higherGoals", "settings")
    val shouldShowBottomBar = currentRoute in tabRoutes
    
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Main tab screens
            composable("home") {
                val viewModel: GoalsViewModel = hiltViewModel()
                Home(navController = navController, viewModel = viewModel)
            }
            
            composable("higherGoals") {
                val viewModel: GoalsViewModel = hiltViewModel()
                HigherGoalsScreen(navController = navController, viewModel = viewModel)
            }
            
            composable("settings") {
                val viewModel: GoalsViewModel = hiltViewModel()
                SettingsScreen(navController = navController, viewModel = viewModel)
            }
            
            // Goal management screens
            composable(
                "edit?id={id}&targetMonth={targetMonth}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("targetMonth") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val viewModel: GoalsViewModel = hiltViewModel()
                val idString = backStackEntry.arguments?.getString("id")
                val targetMonth = backStackEntry.arguments?.getInt("targetMonth") ?: 0
                val id = if (idString != null && idString != "null") {
                    UUID.fromString(idString)
                } else null
                
                GoalEditForm(
                    navController = navController,
                    viewModel = viewModel,
                    goalId = id,
                    targetMonth = targetMonth
                )
            }
            
            // Check-in screens
            composable(
                "checkin/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val viewModel: GoalsViewModel = hiltViewModel()
                val id = UUID.fromString(backStackEntry.arguments?.getString("id"))
                CheckInScreen(navController = navController, viewModel = viewModel, goalId = id)
            }
            
            // Monthly review screens
            composable(
                "monthlyReview/{year}/{month}",
                arguments = listOf(
                    navArgument("year") { type = NavType.IntType },
                    navArgument("month") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val viewModel: GoalsViewModel = hiltViewModel()
                val year = backStackEntry.arguments?.getInt("year") ?: 2024
                val month = backStackEntry.arguments?.getInt("month") ?: 1
                MonthlyReviewWizard(
                    navController = navController,
                    viewModel = viewModel,
                    year = year,
                    month = month
                )
            }
            
            composable(
                "monthlyReviewSummary/{year}/{month}",
                arguments = listOf(
                    navArgument("year") { type = NavType.IntType },
                    navArgument("month") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val viewModel: GoalsViewModel = hiltViewModel()
                val year = backStackEntry.arguments?.getInt("year") ?: 2024
                val month = backStackEntry.arguments?.getInt("month") ?: 1
                MonthlyReviewSummary(
                    navController = navController,
                    viewModel = viewModel,
                    year = year,
                    month = month
                )
            }
        }
    }
}

/**
 * Bottom navigation bar component
 */
@Composable
private fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Star, contentDescription = "Higher Goals") },
            label = { Text("Higher Goals") },
            selected = currentRoute == "higherGoals",
            onClick = { onNavigate("higherGoals") }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") }
        )
    }
}
