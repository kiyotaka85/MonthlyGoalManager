package com.ablomm.monthlygoalmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ablomm.monthlygoalmanager.GoalsViewModel
import com.ablomm.monthlygoalmanager.ui.components.*

/**
 * Settings screen with preferences and app information
 * Optimized with component separation for better maintainability
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: GoalsViewModel
) {
    val isTipsHidden by viewModel.isTipsHidden.collectAsState(initial = false)
    val isHideCompletedGoals by viewModel.isHideCompletedGoals.collectAsState(initial = false)
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SettingsTopAppBar()
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DisplaySettingsCard(
                    isTipsHidden = isTipsHidden,
                    onTipsVisibilityChanged = viewModel::setTipsHidden,
                    isHideCompletedGoals = isHideCompletedGoals,
                    onHideCompletedGoalsChanged = viewModel::setHideCompletedGoals
                )
            }
            
            item {
                AppInfoCard()
            }
            
            item {
                DataManagementCard(
                    onExportData = {
                        // TODO: データエクスポート機能を実装
                    },
                    onImportData = {
                        // TODO: データインポート機能を実装
                    }
                )
            }
        }
    }
}

/**
 * Settings screen top app bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopAppBar() {
    TopAppBar(
        title = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    )
}
