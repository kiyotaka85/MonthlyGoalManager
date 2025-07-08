package com.ablomm.monthlygoalmanager

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
import androidx.compose.material.icons.filled.ContentCopy
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
    var comment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showNoHistoryDialog by remember { mutableStateOf(false) }
    
    val checkInsState = viewModel.getCheckInsForGoal(goalId).collectAsState(initial = emptyList())
    
    // 最後のチェックイン内容を転記する関数
    fun copyLastCheckIn() {
        val checkIns = checkInsState.value
        if (checkIns.isEmpty()) {
            showNoHistoryDialog = true
            return
        }
        
        val lastCheckIn = checkIns.maxByOrNull { it.checkInDate }
        lastCheckIn?.let { 
            progressPercent = it.progressPercent.toString()
            comment = it.comment
        }
    }
    
    // 進捗がない場合の判定
    val currentGoalProgress = goalItemState?.currentProgress ?: 0
    val lastCheckInProgress = checkInsState.value.maxByOrNull { it.checkInDate }?.progressPercent ?: 0
    val hasNoProgressSinceLastCheckIn = currentGoalProgress == lastCheckInProgress && checkInsState.value.isNotEmpty()

    LaunchedEffect(goalId) {
        goalItemState = viewModel.getGoalById(goalId)
        progressPercent = goalItemState?.currentProgress?.toString() ?: "0"
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
                            Text(
                                text = "Current Progress: ${goal.currentProgress}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
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

                        // 最後のチェックイン内容を転記するボタン
                        if (hasNoProgressSinceLastCheckIn) {
                            OutlinedButton(
                                onClick = { copyLastCheckIn() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy from last check-in")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

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
                                val progress = progressPercent.toIntOrNull() ?: 0
                                val checkIn = CheckInItem(
                                    goalId = goalId,
                                    progressPercent = progress,
                                    comment = comment.trim()
                                )
                                viewModel.addCheckIn(checkIn)
                                
                                // Update goal progress
                                goalItemState?.let { goal ->
                                    val updatedGoal = goal.copy(
                                        currentProgress = progress,
                                        isCompleted = progress >= 100
                                    )
                                    viewModel.updateGoalItem(updatedGoal)
                                }
                                
                                navController.popBackStack()
                            },
                            enabled = progressPercent.isNotBlank() && comment.isNotBlank(),
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
        
        // チェックイン履歴がない場合のダイアログ
        if (showNoHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showNoHistoryDialog = false },
                title = { Text("No Check-in History") },
                text = { Text("There are no previous check-ins to copy from. Please create your first check-in manually.") },
                confirmButton = {
                    TextButton(
                        onClick = { showNoHistoryDialog = false }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
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
