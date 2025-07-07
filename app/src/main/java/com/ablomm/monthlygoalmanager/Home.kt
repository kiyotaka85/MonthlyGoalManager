package com.ablomm.monthlygoalmanager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.UUID
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val goalsViewModel: GoalsViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            Home(navController = navController, viewModel = goalsViewModel)
        }

        composable(
            route = "edit/{goalId}",
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goalIdString = backStackEntry.arguments?.getString("goalId")
            val goalId: UUID? = goalIdString?.let { UUID.fromString(it) }

            GoalEditForm(
                goalId = goalId,
                viewModel = goalsViewModel,
                navController = navController
            )
        }

        composable("edit") {
            GoalEditForm(
                goalId = null,
                viewModel = goalsViewModel,
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Home(navController: NavHostController, viewModel: GoalsViewModel) {
    val goalListState = viewModel.goalList.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Goals - June 2025") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("edit")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(
                items = goalListState.value,
                key = { it.id }
            ) { goalItem ->

                val dismissState = rememberSwipeToDismissBoxState(
                    positionalThreshold = { it * 0.25f },
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            viewModel.deleteGoalItem(goalItem)
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.StartToEnd,
                            SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .background(color, shape = RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                    }
                ) {
                    GoalCard(goalItem = goalItem, navController = navController)
                }

                HorizontalDivider()
            }
        }
    }
}

@Composable
fun GoalCard(
    goalItem: GoalItem,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable {
                navController.navigate("edit/${goalItem.id}")
            },
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
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GoalTextArea(
                        modifier = Modifier.weight(1f),
                        title = goalItem.title,
                        description = goalItem.detailedDescription
                    )
                    GoalStatusArea(
                        modifier = Modifier.width(64.dp),
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