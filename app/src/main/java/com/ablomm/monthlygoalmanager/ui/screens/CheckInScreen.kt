package com.ablomm.monthlygoalmanager.ui.screens

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
import com.ablomm.monthlygoalmanager.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Check-in screen for goal progress tracking
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    navController: NavHostController,
    viewModel: GoalsViewModel,
    goalId: UUID
) {
    var goal by remember { mutableStateOf<GoalItem?>(null) }
    var checkIns by remember { mutableStateOf<List<CheckInItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddCheckIn by remember { mutableStateOf(false) }
    var newCheckInText by remember { mutableStateOf("") }
    var newCheckInProgress by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    LaunchedEffect(goalId) {
        try {
            goal = viewModel.getGoalById(goalId)
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load goal: ${e.message}"
            showError = true
            isLoading = false
        }
    }
    
    val checkInsState by viewModel.getCheckInsForGoal(goalId).collectAsState(initial = emptyList())
    
    LaunchedEffect(checkInsState) {
        checkIns = checkInsState.sortedByDescending { it.timestamp }
    }
    
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { 
                    showError = false
                    navController.popBackStack()
                }) {
                    Text("OK")
                }
            }
        )
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        goal?.let { currentGoal ->
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Check-in: ${currentGoal.title}") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showAddCheckIn = true }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Add Check-in")
                    }
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        GoalSummaryCard(goal = currentGoal)
                    }
                    
                    item {
                        CheckInHistoryHeader(count = checkIns.size)
                    }
                    
                    if (checkIns.isEmpty()) {
                        item {
                            EmptyCheckInState()
                        }
                    } else {
                        items(checkIns) { checkIn ->
                            CheckInItemCard(
                                checkIn = checkIn,
                                onDelete = { viewModel.deleteCheckIn(it) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddCheckIn) {
        AddCheckInDialog(
            currentProgress = newCheckInProgress,
            onProgressChange = { newCheckInProgress = it },
            note = newCheckInText,
            onNoteChange = { newCheckInText = it },
            onDismiss = { 
                showAddCheckIn = false
                newCheckInText = ""
                newCheckInProgress = ""
            },
            onConfirm = {
                val progress = newCheckInProgress.toIntOrNull() ?: 0
                val checkIn = CheckInItem(
                    id = UUID.randomUUID(),
                    goalId = goalId,
                    timestamp = System.currentTimeMillis(),
                    progress = progress,
                    note = newCheckInText
                )
                viewModel.addCheckIn(checkIn)
                viewModel.updateGoalProgress(goalId, progress)
                showAddCheckIn = false
                newCheckInText = ""
                newCheckInProgress = ""
            }
        )
    }
}

@Composable
private fun GoalSummaryCard(goal: GoalItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = goal.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current Progress",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "${goal.currentProgress}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = if (goal.isCompleted) "Completed" else "In Progress",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (goal.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckInHistoryHeader(count: Int) {
    Text(
        text = "Check-in History ($count)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun EmptyCheckInState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📝",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No check-ins yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to add your first check-in",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CheckInItemCard(
    checkIn: CheckInItem,
    onDelete: (CheckInItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${checkIn.progress}%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatTimestamp(checkIn.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(
                    onClick = { onDelete(checkIn) }
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (checkIn.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = checkIn.note,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun AddCheckInDialog(
    currentProgress: String,
    onProgressChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Check-in") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentProgress,
                    onValueChange = onProgressChange,
                    label = { Text("Progress (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = currentProgress.toIntOrNull() != null && 
                         currentProgress.toIntOrNull()!! in 0..100
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTimestamp(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
    return localDateTime.format(formatter)
}
