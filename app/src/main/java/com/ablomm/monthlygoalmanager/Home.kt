package com.ablomm.monthlygoalmanager

import android.media.Image
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.time.format.TextStyle
import java.util.UUID

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val goalsViewModel: GoalsViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            Home(navController = navController, viewModel = goalsViewModel)
        }

        composable(
            route = "edit/{goalId}",
            arguments = listOf(navArgument("goalId") {type = NavType.StringType})
        ) { backStackEntry ->
            val goalIdString = backStackEntry.arguments?.getString("goalId")
            val goalId: UUID? = goalIdString?.let {UUID.fromString(it)}
            
            GoalEditForm(
                goalId = goalId,
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Goals - June 2025") })
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            items(viewModel.goalList.value) {
                GoalCard(goalItem = it, navController = navController)
            }

        }
    }
}

@Composable
fun GoalCard(goalItem: GoalItem,
             modifier: Modifier = Modifier,
             navController: NavHostController
    ) {
        Card(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clickable {
                    navController.navigate("edit/${goalItem.id.toString()}")
                },
            colors = CardDefaults.cardColors(
                containerColor = Color.White // Post-it風の黄色
            ),
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
                            modifier = Modifier
                                .weight(1f),
                            title = goalItem.title,
                            description = goalItem.detailedDescription
                        )

                        GoalStatusArea(
                            modifier = Modifier
                                .width(64.dp),
                            statusEmoji = when (goalItem.currentProgress) {
                                0 -> "🆕"
                                100 -> "✅"
                                else -> "⏳"
                            },
                            progress = goalItem.currentProgress,
                        )
                    }

                }
            }
        }
    }

@Preview(showBackground = true)
@Composable
fun GoalTextArea(
    modifier: Modifier = Modifier,
    title: String = "Sample Goal Title",
    description: String? = "Sample This goal is to achieve my personal growth and earn profits to support my family"
) {
    Column(
        modifier = modifier
    ) {

        //Goal Title
        Text(
            text = title,
            fontWeight = FontWeight.Bold
        )

        //Goal Description
        if (description != null) {
            Text(
                text = description,
                fontSize = 12.sp
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
        Text(
            text = statusEmoji,
            fontSize = 18.sp
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = "$progress %",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}