package com.ablomm.monthlygoalmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.focus.focusRequester
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditForm(
    goalId: UUID?,
    viewModel: GoalsViewModel,
    navController: NavHostController,
    targetMonth: Int? = null
) {
    var goalItemState by remember { mutableStateOf<GoalItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // 上位目標のリストを取得
    val higherGoals by viewModel.higherGoalList.collectAsState(initial = emptyList())

    // LaunchedEffectを使って、初回描画時またはgoalIdが変更された時に一度だけ実行
    LaunchedEffect(key1 = goalId) {
        if (goalId == null) {
            // 新規作成モード
            goalItemState = GoalItem(
                id = UUID.randomUUID(),
                title = "",
                detailedDescription = "",
                targetMonth = targetMonth ?: 2025007, // デフォルトは現在月
                targetValue = "",
                currentProgress = 0,
                priority = GoalPriority.Middle,
                isCompleted = false,
                displayOrder = 0
            )
        } else {
            // 編集モード
            goalItemState = viewModel.getGoalById(goalId)
        }
        isLoading = false
    }

    var dropMenuExpanded by remember { mutableStateOf(false) }
    val scrollPosition = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (goalId == null) "Add New Goal" else "Edit Goal")
                },
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
        } else if (goalItemState != null) {
            val focusManager = LocalFocusManager.current
            var titleFieldFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
            var descFieldFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
            var targetValueFieldFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(scrollPosition)
                    .fillMaxSize()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        //Text(
                        //    text = "Basic Information",
                        //    style = MaterialTheme.typography.titleMedium,
                        //    modifier = Modifier.padding(bottom = 16.dp)
                        //)

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .focusRequester(titleFieldFocusRequester),
                            value = goalItemState!!.title,
                            onValueChange = { goalItemState = goalItemState!!.copy(title = it) },
                            label = { Text("Goal Title") },
                            placeholder = { Text("e.g., Read 30 minutes daily") },
                            minLines = 2,
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { descFieldFocusRequester.requestFocus() }
                            )
                        )

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .focusRequester(descFieldFocusRequester),
                            value = goalItemState!!.detailedDescription ?: "",
                            onValueChange = { goalItemState = goalItemState!!.copy(detailedDescription = it) },
                            label = { Text("Detailed Description (Optional)") },
                            placeholder = { Text("Enter details or background of your goal") },
                            minLines = 3,
                            maxLines = 5,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { targetValueFieldFocusRequester.requestFocus() }
                            )
                        )

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .focusRequester(targetValueFieldFocusRequester),
                            value = goalItemState!!.targetValue,
                            onValueChange = { goalItemState = goalItemState!!.copy(targetValue = it) },
                            label = { Text("Target Value") },
                            placeholder = { Text("e.g., 30 books, 10kg, daily") },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            )
                        )

                        // 上位目標選択
                        if (higherGoals.isNotEmpty()) {
                            Text(
                                text = "Higher Goal (Optional)",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            val selectedHigherGoal = higherGoals.find { it.id == goalItemState!!.higherGoalId }
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .clickable {
                                        navController.navigate("higherGoals")
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    if (selectedHigherGoal != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(android.graphics.Color.parseColor(selectedHigherGoal.color)))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = selectedHigherGoal.title,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    goalItemState = goalItemState!!.copy(higherGoalId = null)
                                                }
                                            ) {
                                                Text("Remove", color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "No higher goal selected",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tap to manage higher goals",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = goalItemState!!.currentProgress.toString(),
                            onValueChange = { text ->
                                val progress = text.toIntOrNull() ?: 0
                                val clampedProgress = progress.coerceIn(0, 100)
                                goalItemState = goalItemState!!.copy(currentProgress = clampedProgress)
                            },
                            label = { Text("Current Progress (%)") },
                            placeholder = { Text("Enter value between 0-100") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        ExposedDropdownMenuBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            expanded = dropMenuExpanded,
                            onExpandedChange = { dropMenuExpanded = !dropMenuExpanded }
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .menuAnchor(
                                        type = MenuAnchorType.PrimaryEditable,
                                        enabled = true
                                    )
                                    .fillMaxWidth(),
                                value = when (goalItemState!!.priority) {
                                    GoalPriority.Low -> "Low"
                                    GoalPriority.Middle -> "Medium"
                                    GoalPriority.High -> "High"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Priority") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropMenuExpanded)
                                }
                            )
                            DropdownMenu(
                                expanded = dropMenuExpanded,
                                onDismissRequest = { dropMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("High") },
                                    onClick = {
                                        goalItemState = goalItemState!!.copy(priority = GoalPriority.High)
                                        dropMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Medium") },
                                    onClick = {
                                        goalItemState = goalItemState!!.copy(priority = GoalPriority.Middle)
                                        dropMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Low") },
                                    onClick = {
                                        goalItemState = goalItemState!!.copy(priority = GoalPriority.Low)
                                        dropMenuExpanded = false
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = goalItemState!!.isCompleted,
                                onCheckedChange = { isChecked ->
                                    goalItemState = goalItemState!!.copy(
                                        isCompleted = isChecked,
                                        currentProgress = if (isChecked) 100 else goalItemState!!.currentProgress
                                    )
                                }
                            )
                            Text(
                                text = "Completed",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                // 保存ボタン
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                goalItemState?.let { currentGoal ->
                                    if (goalId == null) {
                                        viewModel.addGoalItem(currentGoal)
                                    } else {
                                        viewModel.updateGoalItem(currentGoal)
                                    }
                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = goalItemState!!.title.isNotBlank()
                        ) {
                            Text(if (goalId == null) "Add Goal" else "Save Changes")
                        }
                        
                        // 削除ボタン (編集モード時のみ表示)
                        if (goalId != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            var showDeleteDialog by remember { mutableStateOf(false) }
                            
                            Button(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Delete Goal", color = Color.White)
                            }
                            
                            // 削除確認ダイアログ
                            if (showDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteDialog = false },
                                    title = { Text("Delete Goal") },
                                    text = { Text("Are you sure you want to delete this goal? This action cannot be undone.") },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                goalItemState?.let { currentGoal ->
                                                    viewModel.deleteGoalItem(currentGoal)
                                                    navController.popBackStack()
                                                }
                                            }
                                        ) {
                                            Text("Delete", color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteDialog = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("An error occurred")
            }
        }
    }
}
