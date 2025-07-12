package com.ablomm.monthlygoalmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ablomm.monthlygoalmanager.*
import java.util.*

/**
 * Goal Edit Form screen for creating and editing goals
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditForm(
    navController: NavHostController,
    viewModel: GoalsViewModel,
    goalId: UUID?,
    targetMonth: Int
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetValue by remember { mutableStateOf("") }
    var currentProgress by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(GoalPriority.Middle) }
    var category by remember { mutableStateOf("") }
    var displayOrder by remember { mutableStateOf("") }
    var higherGoalId by remember { mutableStateOf<UUID?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val higherGoals by viewModel.higherGoalList.collectAsState(initial = emptyList())
    val isEditMode = goalId != null
    
    // Load existing goal data if editing
    LaunchedEffect(goalId) {
        if (goalId != null) {
            isLoading = true
            try {
                val goal = viewModel.getGoalById(goalId)
                goal?.let {
                    title = it.title
                    description = it.description
                    targetValue = it.targetValue.toString()
                    currentProgress = it.currentProgress.toString()
                    isCompleted = it.isCompleted
                    priority = it.priority
                    category = it.category
                    displayOrder = it.displayOrder.toString()
                    higherGoalId = it.higherGoalId
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load goal: ${e.message}"
                showError = true
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Goal" else "New Goal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        TextButton(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
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
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Basic Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Basic Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Goal Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                        
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                
                // Progress Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Progress Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = targetValue,
                            onValueChange = { targetValue = it },
                            label = { Text("Target Value") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = currentProgress,
                            onValueChange = { currentProgress = it },
                            label = { Text("Current Progress (%)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isCompleted,
                                onCheckedChange = { isCompleted = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark as completed")
                        }
                    }
                }
                
                // Goal Settings Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Goal Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Priority Selection
                        PrioritySelector(
                            selectedPriority = priority,
                            onPriorityChange = { priority = it }
                        )
                        
                        // Higher Goal Selection
                        HigherGoalSelector(
                            higherGoals = higherGoals,
                            selectedHigherGoalId = higherGoalId,
                            onHigherGoalChange = { higherGoalId = it }
                        )
                        
                        OutlinedTextField(
                            value = displayOrder,
                            onValueChange = { displayOrder = it },
                            label = { Text("Display Order") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val goal = GoalItem(
                                id = goalId ?: UUID.randomUUID(),
                                title = title,
                                description = description,
                                targetValue = targetValue.toIntOrNull() ?: 0,
                                currentProgress = currentProgress.toIntOrNull() ?: 0,
                                isCompleted = isCompleted,
                                priority = priority,
                                category = category,
                                targetMonth = if (targetMonth > 0) targetMonth else {
                                    val now = java.time.YearMonth.now()
                                    now.year * 1000 + now.monthValue
                                },
                                displayOrder = displayOrder.toIntOrNull() ?: 0,
                                higherGoalId = higherGoalId
                            )
                            
                            if (isEditMode) {
                                viewModel.updateGoal(goal)
                            } else {
                                viewModel.addGoal(goal)
                            }
                            
                            navController.popBackStack()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = title.isNotBlank()
                    ) {
                        Text(if (isEditMode) "Update" else "Create")
                    }
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete this goal? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        goalId?.let { id ->
                            viewModel.deleteGoal(GoalItem(
                                id = id,
                                title = title,
                                description = description,
                                targetValue = targetValue.toIntOrNull() ?: 0,
                                currentProgress = currentProgress.toIntOrNull() ?: 0,
                                isCompleted = isCompleted,
                                priority = priority,
                                category = category,
                                targetMonth = targetMonth,
                                displayOrder = displayOrder.toIntOrNull() ?: 0,
                                higherGoalId = higherGoalId
                            ))
                        }
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Error Dialog
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun PrioritySelector(
    selectedPriority: GoalPriority,
    onPriorityChange: (GoalPriority) -> Unit
) {
    Column {
        Text(
            text = "Priority",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GoalPriority.values().forEach { priority ->
                FilterChip(
                    onClick = { onPriorityChange(priority) },
                    label = { Text(priority.name) },
                    selected = selectedPriority == priority,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HigherGoalSelector(
    higherGoals: List<HigherGoal>,
    selectedHigherGoalId: UUID?,
    onHigherGoalChange: (UUID?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedGoal = higherGoals.find { it.id == selectedHigherGoalId }
    
    Column {
        Text(
            text = "Higher Goal (optional)",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedGoal?.title ?: "None",
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        onHigherGoalChange(null)
                        expanded = false
                    }
                )
                
                higherGoals.forEach { goal ->
                    DropdownMenuItem(
                        text = { Text(goal.title) },
                        onClick = {
                            onHigherGoalChange(goal.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
