package com.ablomm.monthlygoalmanager

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
fun CheckInScreen(
    goalId: UUID,
    viewModel: GoalsViewModel,
    navController: NavHostController
) {
    var goalItemState by remember { mutableStateOf<GoalItem?>(null) }
    var progressPercent by remember { mutableStateOf("") }
    var numericValue by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    var savedCheckIn by remember { mutableStateOf<CheckInItem?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val checkInsState = viewModel.getCheckInsForGoal(goalId).collectAsState(initial = emptyList())

    LaunchedEffect(goalId) {
        goalItemState = viewModel.getGoalById(goalId)
        progressPercent = goalItemState?.currentProgress?.toString() ?: "0"
        numericValue = goalItemState?.currentNumericValue?.toString() ?: ""
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check-in") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Goal Information Card
                goalItemState?.let { goal ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // 数値目標とシンプル目標で表示を分ける
                            if (goal.goalType == GoalType.NUMERIC) {
                                Text(
                                    text = "現在の進捗: ${goal.currentNumericValue?.toInt() ?: 0} / ${goal.targetNumericValue?.toInt() ?: 0} ${goal.unit ?: ""} (${goal.currentProgress}%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            } else {
                                Text(
                                    text = "現在の進捗: ${goal.currentProgress}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }

                            LinearProgressIndicator(
                                progress = goal.currentProgress / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // New Check-in Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "New Check-in",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        goalItemState?.let { goal ->
                            if (goal.goalType == GoalType.NUMERIC) {
                                // 数値目標の場合：単位付きの数値入力
                                OutlinedTextField(
                                    value = numericValue,
                                    onValueChange = { text ->
                                        val value = text.toDoubleOrNull()
                                        if (value == null && text.isNotEmpty()) return@OutlinedTextField
                                        if (value != null && value < 0) return@OutlinedTextField
                                        numericValue = text
                                    },
                                    label = { Text("現在の数値 (${goal.unit ?: ""})") },
                                    placeholder = { Text("例：${goal.targetNumericValue?.toInt() ?: 100}") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                // シンプル目標の場合：進捗率入力
                                OutlinedTextField(
                                    value = progressPercent,
                                    onValueChange = { text ->
                                        val progress = text.toIntOrNull()
                                        if (progress == null && text.isNotEmpty()) return@OutlinedTextField
                                        if (progress != null && (progress < 0 || progress > 100)) return@OutlinedTextField
                                        progressPercent = text
                                    },
                                    label = { Text("Progress (%)") },
                                    placeholder = { Text("Enter progress percentage (0-100)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            label = { Text("Comments") },
                            placeholder = { Text("What did you accomplish? How do you feel?") },
                            minLines = 3,
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                goalItemState?.let { goal ->
                                    val progress = if (goal.goalType == GoalType.NUMERIC) {
                                        // 数値目標の場合：入力値から進捗率を計算
                                        val currentValue = numericValue.toDoubleOrNull() ?: 0.0
                                        val targetValue = goal.targetNumericValue ?: 1.0
                                        if (targetValue > 0) {
                                            ((currentValue / targetValue) * 100).coerceIn(0.0, 100.0).toInt()
                                        } else {
                                            0
                                        }
                                    } else {
                                        // シンプル目標の場合：直接進捗率を使用
                                        progressPercent.toIntOrNull() ?: 0
                                    }

                                    val checkIn = CheckInItem(
                                        goalId = goalId,
                                        progressPercent = progress,
                                        comment = comment.trim()
                                    )
                                    viewModel.addCheckIn(checkIn)

                                    // Update goal progress
                                    val updatedGoal = if (goal.goalType == GoalType.NUMERIC) {
                                        goal.copy(
                                            currentNumericValue = numericValue.toDoubleOrNull() ?: 0.0,
                                            currentProgress = progress,
                                            isCompleted = progress >= 100
                                        )
                                    } else {
                                        goal.copy(
                                            currentProgress = progress,
                                            isCompleted = progress >= 100
                                        )
                                    }
                                    viewModel.updateGoalItem(updatedGoal)

                                    // Show completion dialog for all check-ins
                                    savedCheckIn = checkIn
                                    showCompletionDialog = true
                                }
                            },
                            enabled = goalItemState?.let { goal ->
                                if (goal.goalType == GoalType.NUMERIC) {
                                    numericValue.isNotBlank() && comment.isNotBlank()
                                } else {
                                    progressPercent.isNotBlank() && comment.isNotBlank()
                                }
                            } ?: false,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Check-in")
                        }
                    }
                }

                // Check-in History
                if (checkInsState.value.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Check-in History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            LazyColumn(
                                modifier = Modifier.heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(checkInsState.value) { checkIn ->
                                    CheckInHistoryItem(checkIn = checkIn)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Check-in Completion Dialog
    if (showCompletionDialog && savedCheckIn != null && goalItemState != null) {
        CheckInCompletionDialog(
            goal = goalItemState!!,
            checkIn = savedCheckIn!!,
            onShare = { shareText ->
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "進捗を共有"))
                showCompletionDialog = false
                navController.popBackStack()
            },
            onDismiss = {
                showCompletionDialog = false
                navController.popBackStack()
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CheckInHistoryItem(checkIn: CheckInItem) {
    val dateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(checkIn.checkInDate),
        ZoneId.systemDefault()
    )
    val formattedDate = dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${checkIn.progressPercent}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            if (checkIn.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = checkIn.comment,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun CheckInCompletionDialog(
    goal: GoalItem,
    checkIn: CheckInItem,
    onShare: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isGoalCompleted = checkIn.progressPercent >= 100

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isGoalCompleted) "🎉 目標達成！" else "✅ チェックイン完了！ 今日も一歩前進🥳",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isGoalCompleted)
                        "おめでとうございます！目標を達成しました！"
                    else
                        "チェックインが完了しました。",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 記入内容の表示
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "記入内容",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("目標:")
                            Text(goal.title, fontWeight = FontWeight.Medium)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("進捗:")
                            Text("${checkIn.progressPercent}%", fontWeight = FontWeight.Medium)
                        }

                        if (checkIn.comment.isNotBlank()) {
                            Column {
                                Text("コメント:", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = checkIn.comment,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("閉じる")
                }

                Button(
                    onClick = {
                        val shareText = buildString {
                            if (isGoalCompleted) {
                                appendLine("🎉 目標達成しました！")
                            } else {
                                appendLine("📈 進捗更新")
                            }
                            appendLine()
                            appendLine("目標: ${goal.title}")
                            appendLine("進捗: ${checkIn.progressPercent}%")
                            if (checkIn.comment.isNotBlank()) {
                                appendLine()
                                appendLine("💭 ${checkIn.comment}")
                            }
                            appendLine()
                            appendLine("#目標達成 #進捗 #モチベーション")
                        }
                        onShare(shareText)
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("共有")
                }
            }
        }
    )
}
