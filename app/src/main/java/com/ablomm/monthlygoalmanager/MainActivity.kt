package com.ablomm.monthlygoalmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ablomm.monthlygoalmanager.ui.theme.MonthlyGoalManagerTheme
import com.ablomm.monthlygoalmanager.ui.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Entry point of the application
 * Configures the app theme and launches the navigation
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonthlyGoalManagerTheme {
                AppNavigation()
            }
        }
    }
}