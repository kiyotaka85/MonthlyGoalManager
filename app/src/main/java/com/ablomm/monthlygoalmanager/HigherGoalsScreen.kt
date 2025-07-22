package com.ablomm.monthlygoalmanager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HigherGoalsScreen(
    navController: NavHostController,
    viewModel: GoalsViewModel = hiltViewModel(),
    goalId: UUID? = null // 選択モード用の目標ID
) {
    val allHigherGoals by viewModel.higherGoalList.collectAsState(initial = emptyList())
    val isHideCompleted by viewModel.isHideCompletedHigherGoals.collectAsState(initial = false)

    // フィルタリングされた上位目標リスト
    val higherGoals = if (isHideCompleted) {
        allHigherGoals.filter { !it.isCompleted }
    } else {
        allHigherGoals
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<HigherGoal?>(null) }
    var goalToDelete by remember { mutableStateOf<HigherGoal?>(null) }

    val isSelectionMode = goalId != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSelectionMode) "Select Higher Goal" else "Higher Goals & Visions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // アーカイブトグルボタン
                    if (!isSelectionMode) {
                        IconButton(
                            onClick = {
                                viewModel.setHideCompletedHigherGoals(!isHideCompleted)
                            }
                        ) {
                            Icon(
                                imageVector = if (isHideCompleted) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isHideCompleted) "Show completed" else "Hide completed",
                                tint = if (isHideCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Higher Goal")
            }
        }
    ) { paddingValues ->
        if (higherGoals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isHideCompleted && allHigherGoals.isNotEmpty()) "🏆" else "🎯",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isHideCompleted && allHigherGoals.isNotEmpty())
                            "All higher goals completed!" else "No higher goals yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isHideCompleted && allHigherGoals.isNotEmpty())
                            "Toggle visibility to see completed goals" else
                            "Create higher goals to organize your monthly goals",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(higherGoals) { higherGoal ->
                    HigherGoalCard(
                        higherGoal = higherGoal,
                        onEdit = {
                            editingGoal = higherGoal
                            showEditDialog = true
                        },
                        onDelete = {
                            goalToDelete = higherGoal
                            showDeleteDialog = true
                        },
                        onToggleCompletion = {
                            viewModel.toggleHigherGoalCompletion(higherGoal)
                        },
                        isSelectionMode = isSelectionMode,
                        onSelect = {
                            if (goalId != null) {
                                // 選択された上位目標IDを前の画面に渡す
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "selected_higher_goal_id",
                                    higherGoal.id.toString()
                                )
                                navController.popBackStack()
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddHigherGoalDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, description, color ->
                val higherGoal = HigherGoal(
                    title = title,
                    description = description,
                    color = color
                )
                viewModel.addHigherGoal(higherGoal)
                showAddDialog = false
            }
        )
    }

    // 編集ダイアログ
    if (showEditDialog && editingGoal != null) {
        EditHigherGoalDialog(
            higherGoal = editingGoal!!,
            onDismiss = {
                showEditDialog = false
                editingGoal = null
            },
            onUpdate = { updatedGoal ->
                viewModel.updateHigherGoal(updatedGoal)
                showEditDialog = false
                editingGoal = null
            }
        )
    }

    // 削除確認ダイアログ
    if (showDeleteDialog && goalToDelete != null) {
        DeleteHigherGoalDialog(
            higherGoal = goalToDelete!!,
            onDismiss = {
                showDeleteDialog = false
                goalToDelete = null
            },
            onConfirm = {
                viewModel.deleteHigherGoal(goalToDelete!!)
                showDeleteDialog = false
                goalToDelete = null
            }
        )
    }
}

@Composable
fun HigherGoalCard(
    higherGoal: HigherGoal,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleCompletion: () -> Unit = {},
    isSelectionMode: Boolean = false,
    onSelect: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isSelectionMode) {
                    onSelect()
                }
            },
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(higherGoal.color)))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = higherGoal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!higherGoal.description.isNullOrBlank()) {
                            Text(
                                text = higherGoal.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (!isSelectionMode) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Tap to select",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 完了トグルスイッチ
            if (!isSelectionMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = higherGoal.isCompleted,
                        onCheckedChange = {
                            onToggleCompletion()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AddHigherGoalDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#2196F3") }
    
    val colors = listOf(
        "#2196F3", "#4CAF50", "#FF9800", "#9C27B0", 
        "#F44336", "#00BCD4", "#FFEB3B", "#795548"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Higher Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Color", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .clickable { selectedColor = color }
                                .then(
                                    if (selectedColor == color) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                    } else Modifier
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(title, description, selectedColor) },
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
fun EditHigherGoalDialog(
    higherGoal: HigherGoal,
    onDismiss: () -> Unit,
    onUpdate: (HigherGoal) -> Unit
) {
    var title by remember { mutableStateOf(higherGoal.title) }
    var description by remember { mutableStateOf(higherGoal.description ?: "") }
    var selectedColor by remember { mutableStateOf(higherGoal.color) }

    val colors = listOf(
        "#2196F3", "#4CAF50", "#FF9800", "#9C27B0",
        "#F44336", "#00BCD4", "#FFEB3B", "#795548"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Higher Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Color", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .clickable { selectedColor = color }
                                .then(
                                    if (selectedColor == color) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                    } else Modifier
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdate(
                        higherGoal.copy(
                            title = title,
                            description = if (description.isBlank()) null else description,
                            color = selectedColor
                        )
                    )
                },
                enabled = title.isNotBlank()
            ) {
                Text("Update")
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
fun DeleteHigherGoalDialog(
    higherGoal: HigherGoal,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete this higher goal?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
