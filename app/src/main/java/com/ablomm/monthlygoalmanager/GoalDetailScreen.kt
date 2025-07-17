package com.ablomm.monthlygoalmanager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GoalDetailScreen(
    goalId: UUID,
    viewModel: GoalsViewModel,
    navController: NavHostController
) {
    var goalItem by remember { mutableStateOf<GoalItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val checkInsState = viewModel.getCheckInsForGoal(goalId).collectAsState(initial = emptyList())
    val actionStepsState = viewModel.getActionStepsForGoal(goalId).collectAsState(initial = emptyList())
    val higherGoalsState = viewModel.higherGoalList.collectAsState(initial = emptyList())

    LaunchedEffect(goalId) {
        goalItem = viewModel.getGoalById(goalId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("目標詳細") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("goalEdit/$goalId") }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "編集")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (goalItem != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 目標基本情報カード
                item {
                    GoalBasicInfoCard(
                        goal = goalItem!!,
                        higherGoals = higherGoalsState.value
                    )
                }

                // 進捗カード
                item {
                    GoalProgressCard(
                        goal = goalItem!!,
                        checkIns = checkInsState.value
                    )
                }

                // Action Stepsカード
                if (actionStepsState.value.isNotEmpty()) {
                    item {
                        ActionStepsCard(actionSteps = actionStepsState.value)
                    }
                }

                // チェックイン履歴カード
                if (checkInsState.value.isNotEmpty()) {
                    item {
                        CheckInHistoryCard(checkIns = checkInsState.value)
                    }
                }

                // アクションボタン
                item {
                    GoalActionButtons(
                        goalId = goalId,
                        navController = navController
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("目標が見つかりませんでした")
            }
        }
    }
}

@Composable
fun GoalBasicInfoCard(
    goal: GoalItem,
    higherGoals: List<HigherGoal>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // 数値目標の詳細（常に表示）
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("目標値:", fontWeight = FontWeight.Medium)
                Text("${goal.targetNumericValue} ${goal.unit}")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("現在値:", fontWeight = FontWeight.Medium)
                Text("${goal.currentNumericValue} ${goal.unit}")
            }

            // 優先度
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("優先度:", fontWeight = FontWeight.Medium)
                Text(
                    text = when (goal.priority) {
                        GoalPriority.High -> "高"
                        GoalPriority.Middle -> "中"
                        GoalPriority.Low -> "低"
                    }
                )
            }

            // 上位目標
            val higherGoal = higherGoals.find { it.id == goal.higherGoalId }
            if (higherGoal != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("上位目標:", fontWeight = FontWeight.Medium)
                    Text(higherGoal.title)
                }
            }

            // ご褒美
            if (!goal.celebration.isNullOrBlank()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🎁 ご褒美:", fontWeight = FontWeight.Medium)
                    Text(
                        text = goal.celebration,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 詳細説明
            if (!goal.detailedDescription.isNullOrBlank()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("詳細説明:", fontWeight = FontWeight.Medium)
                    Text(
                        text = goal.detailedDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GoalProgressCard(
    goal: GoalItem,
    checkIns: List<CheckInItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "進捗状況",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("現在の進捗:")
                Text(
                    text = "${goal.currentProgress}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = goal.currentProgress / 100f,
                modifier = Modifier.fillMaxWidth()
            )

            if (goal.isCompleted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700)
                    )
                    Text(
                        text = "完了済み",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            // 統計情報
            if (checkIns.isNotEmpty()) {
                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${checkIns.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "チェックイン回数",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val avgProgress = checkIns.map { it.progressPercent }.average().toInt()
                        Text(
                            text = "${avgProgress}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "平均進捗",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionStepsCard(actionSteps: List<ActionStep>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Action Steps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            actionSteps.sortedBy { it.order }.forEach { step ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (step.isCompleted) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                    } else {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }

                    Text(
                        text = step.title,
                        modifier = Modifier.weight(1f),
                        style = if (step.isCompleted) {
                            MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray
                            )
                        } else {
                            MaterialTheme.typography.bodyMedium
                        }
                    )
                }
            }

            val completedSteps = actionSteps.count { it.isCompleted }
            val totalSteps = actionSteps.size

            Text(
                text = "完了: $completedSteps / $totalSteps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CheckInHistoryCard(checkIns: List<CheckInItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "チェックイン履歴",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            checkIns.take(5).forEach { checkIn ->
                val dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(checkIn.checkInDate),
                    ZoneId.systemDefault()
                )
                val formattedDate = dateTime.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${checkIn.progressPercent}%",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        if (checkIn.comment.isNotBlank()) {
                            Text(
                                text = checkIn.comment,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            if (checkIns.size > 5) {
                Text(
                    text = "他 ${checkIns.size - 5} 件のチェックイン",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GoalActionButtons(
    goalId: UUID,
    navController: NavHostController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { navController.navigate("checkIn/$goalId") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("チェックイン")
        }

        OutlinedButton(
            onClick = { navController.navigate("goalEdit/$goalId") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("編集")
        }
    }
}
