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
import androidx.compose.material3.Button
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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
                isCompleted = false
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
                    Text(if (goalId == null) "新しい目標" else "目標を編集")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
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
                        Text(
                            text = "基本情報",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = goalItemState!!.title,
                            onValueChange = { goalItemState = goalItemState!!.copy(title = it) },
                            label = { Text("目標のタイトル") },
                            placeholder = { Text("例: 毎日30分読書する") },
                            minLines = 2,
                            maxLines = 3
                        )

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = goalItemState!!.detailedDescription ?: "",
                            onValueChange = { goalItemState = goalItemState!!.copy(detailedDescription = it) },
                            label = { Text("詳細説明（任意）") },
                            placeholder = { Text("目標の詳細や背景を記入してください") },
                            minLines = 3,
                            maxLines = 5
                        )

                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            value = goalItemState!!.targetValue,
                            onValueChange = { goalItemState = goalItemState!!.copy(targetValue = it) },
                            label = { Text("目標値") },
                            placeholder = { Text("例: 30冊、10kg、毎日") }
                        )

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
                            label = { Text("現在の進捗 (%)") },
                            placeholder = { Text("0-100の数値を入力") },
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
                                    GoalPriority.Low -> "低"
                                    GoalPriority.Middle -> "中"
                                    GoalPriority.High -> "高"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("優先度") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropMenuExpanded)
                                }
                            )
                            DropdownMenu(
                                expanded = dropMenuExpanded,
                                onDismissRequest = { dropMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("高") },
                                    onClick = {
                                        goalItemState = goalItemState!!.copy(priority = GoalPriority.High)
                                        dropMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("中") },
                                    onClick = {
                                        goalItemState = goalItemState!!.copy(priority = GoalPriority.Middle)
                                        dropMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("低") },
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
                                text = "完了済み",
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
                            Text(if (goalId == null) "目標を追加" else "変更を保存")
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("エラーが発生しました")
            }
        }
    }
}

