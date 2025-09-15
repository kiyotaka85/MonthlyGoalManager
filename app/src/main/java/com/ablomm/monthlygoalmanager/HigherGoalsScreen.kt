package com.ablomm.monthlygoalmanager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
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

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(if (isSelectionMode) "Select Higher Goal" else "Higher Goals & Visions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                },
                scrollBehavior = scrollBehavior
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
                    .padding(paddingValues)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    val message = if (isHideCompleted && allHigherGoals.isNotEmpty()) {
                        Triple("🏆", "All Goals Achieved!", "Congratulations! You've completed all your higher goals. Toggle the visibility to see them again.")
                    } else {
                        Triple("🚀", "Ready for a New Vision?", "Let's set some ambitious higher goals. Tap the '+' button to begin your journey.")
                    }

                    Text(text = message.first, fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = message.second,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = message.third,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
            onAdd = { title, description, icon ->
                val higherGoal = HigherGoal(
                    title = title,
                    description = description,
                    icon = icon
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
            .clickable { if (isSelectionMode) onSelect() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = GoalIcons.getIconByName(higherGoal.icon),
                    contentDescription = GoalIcons.getIconDescription(higherGoal.icon),
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = higherGoal.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (!isSelectionMode && higherGoal.isCompleted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (!higherGoal.description.isNullOrBlank()) {
                    Text(
                        text = higherGoal.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelectionMode) {
                Text(
                    text = "Select",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEdit()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(if (higherGoal.isCompleted) "Unarchive" else "Archive")
                            },
                            onClick = {
                                onToggleCompletion()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (higherGoal.isCompleted) Icons.Default.Unarchive else Icons.Default.Archive,
                                    contentDescription = "Archive"
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
                        )
                    }
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
    var selectedIcon by remember { mutableStateOf("EmojiEvents") }

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

                Text("Icon", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(GoalIcons.allIcons) { goalIcon ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedIcon == goalIcon.name)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIcon = goalIcon.name }
                                .border(
                                    width = 2.dp,
                                    color = if (selectedIcon == goalIcon.name) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = goalIcon.icon,
                                contentDescription = goalIcon.description,
                                modifier = Modifier.size(28.dp),
                                tint = if (selectedIcon == goalIcon.name)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(title, description, selectedIcon) },
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
    var selectedIcon by remember { mutableStateOf(higherGoal.icon) }

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

                Text("Icon", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(GoalIcons.allIcons) { goalIcon ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedIcon == goalIcon.name)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIcon = goalIcon.name }
                                .border(
                                    width = 2.dp,
                                    color = if (selectedIcon == goalIcon.name) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = goalIcon.icon,
                                contentDescription = goalIcon.description,
                                modifier = Modifier.size(28.dp),
                                tint = if (selectedIcon == goalIcon.name)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                            icon = selectedIcon
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
