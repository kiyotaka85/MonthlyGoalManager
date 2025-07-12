package com.ablomm.monthlygoalmanager.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ablomm.monthlygoalmanager.*
import java.util.*

/**
 * Higher Goals screen for long-term goal management
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HigherGoalsScreen(
    navController: NavHostController,
    viewModel: GoalsViewModel
) {
    val higherGoals by viewModel.higherGoalList.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<HigherGoal?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Higher Goals") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Higher Goal")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (higherGoals.isEmpty()) {
                item {
                    EmptyHigherGoalsState()
                }
            } else {
                items(higherGoals) { higherGoal ->
                    HigherGoalCard(
                        higherGoal = higherGoal,
                        onEdit = { editingGoal = it },
                        onDelete = { viewModel.deleteHigherGoal(it) }
                    )
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddHigherGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, category ->
                val newGoal = HigherGoal(
                    id = UUID.randomUUID(),
                    title = title,
                    description = description,
                    category = category,
                    createdAt = System.currentTimeMillis()
                )
                viewModel.addHigherGoal(newGoal)
                showAddDialog = false
            }
        )
    }
    
    editingGoal?.let { goal ->
        EditHigherGoalDialog(
            goal = goal,
            onDismiss = { editingGoal = null },
            onConfirm = { title, description, category ->
                val updatedGoal = goal.copy(
                    title = title,
                    description = description,
                    category = category
                )
                viewModel.updateHigherGoal(updatedGoal)
                editingGoal = null
            }
        )
    }
}

@Composable
private fun EmptyHigherGoalsState() {
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
                text = "⭐",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No higher goals yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to add your first higher goal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HigherGoalCard(
    higherGoal: HigherGoal,
    onEdit: (HigherGoal) -> Unit,
    onDelete: (HigherGoal) -> Unit
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = higherGoal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (higherGoal.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = higherGoal.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .background(
                                color = getCategoryColor(higherGoal.category),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = higherGoal.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = { onEdit(higherGoal) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onDelete(higherGoal) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddHigherGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Higher Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                CategorySelector(
                    selectedCategory = category,
                    onCategoryChange = { category = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, description, category) },
                enabled = title.isNotBlank()
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

@Composable
private fun EditHigherGoalDialog(
    goal: HigherGoal,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(goal.title) }
    var description by remember { mutableStateOf(goal.description) }
    var category by remember { mutableStateOf(goal.category) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Higher Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                CategorySelector(
                    selectedCategory = category,
                    onCategoryChange = { category = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, description, category) },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CategorySelector(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    val categories = listOf("Personal", "Career", "Health", "Learning", "Financial", "Relationships", "Other")
    
    Column {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    onClick = { onCategoryChange(category) },
                    label = { Text(category) },
                    selected = selectedCategory == category
                )
            }
        }
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category) {
        "Personal" -> Color(0xFF2196F3)
        "Career" -> Color(0xFF4CAF50)
        "Health" -> Color(0xFFFF9800)
        "Learning" -> Color(0xFF9C27B0)
        "Financial" -> Color(0xFF00BCD4)
        "Relationships" -> Color(0xFFE91E63)
        else -> Color(0xFF607D8B)
    }
}
