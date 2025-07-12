package com.ablomm.monthlygoalmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ablomm.monthlygoalmanager.ui.theme.MonthlyGoalManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonthlyGoalManagerTheme {
                AppNavigation()
                //GoalEditForm(GoalItem(title = "This is my goal that I really want to achieve."))
            }
        }
    }
}