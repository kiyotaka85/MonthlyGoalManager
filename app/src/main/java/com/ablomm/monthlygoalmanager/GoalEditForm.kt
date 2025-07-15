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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.focusRequester
import java.util.UUID
import androidx.compose.material3.Checkbox
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditForm(
    goalId: UUID?,
    viewModel: GoalsViewModel,
    navController: NavHostController,
    targetMonth: Int? = null
) {
    val editingGoalItem by viewModel.editingGoalItem.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    // 上位目標のリストを取得
    val higherGoals by viewModel.higherGoalList.collectAsState(initial = emptyList())

    // LaunchedEffectを使って、初回描画時またはgoalIdが変更された時に一度だけ実行
    LaunchedEffect(key1 = goalId) {
        if (goalId == null) {
            // 新規作成モード
            viewModel.setEditingGoalItem(GoalItem(
                id = UUID.randomUUID(),
                title = "",
                detailedDescription = "",
                targetMonth = targetMonth ?: 2025007,
                goalType = GoalType.NUMERIC, // デフォルトを数値目標に変更
                targetValue = "",
                targetNumericValue = null,
                currentNumericValue = null,
                unit = null,
                currentProgress = 0,
                priority = GoalPriority.Middle,
                isCompleted = false,
                displayOrder = 0
            ))
        } else {
            // 編集モード
            val loaded = viewModel.getGoalById(goalId)
            viewModel.setEditingGoalItem(loaded)
        }
        isLoading = false
    }

    // 上位目標選択結果の受け取り
    LaunchedEffect(navController) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("selected_higher_goal_id")?.observeForever { selectedId ->
            if (selectedId != null) {
                val higherGoalId = UUID.fromString(selectedId)
                editingGoalItem?.let { currentGoal ->
                    viewModel.setEditingGoalItem(currentGoal.copy(higherGoalId = higherGoalId))
                }
                // 使用済みのデータをクリア
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_higher_goal_id")
            }
        }
    }

    val scrollPosition = rememberScrollState()
    val focusManager = LocalFocusManager.current

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
        } else if (editingGoalItem != null) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(scrollPosition)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 目標
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editingGoalItem!!.title,
                    onValueChange = { viewModel.setEditingGoalItem(editingGoalItem!!.copy(title = it)) },
                    label = { Text("目標 *") },
                    placeholder = { Text("例：毎日30分読書する") },
                    minLines = 3,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.clearFocus() }
                    )
                )

                // 目標タイプ選択（トグルタブ）
                GoalTypeToggle(
                    selectedType = editingGoalItem!!.goalType,
                    onTypeChanged = { newType ->
                        viewModel.setEditingGoalItem(editingGoalItem!!.copy(goalType = newType))
                    }
                )

                // 数値目標選択時の追加フィールド
                if (editingGoalItem!!.goalType == GoalType.NUMERIC) {
                    NumericGoalFields(
                        targetValue = editingGoalItem!!.targetNumericValue ?: 0.0,
                        currentValue = editingGoalItem!!.currentNumericValue ?: 0.0,
                        unit = editingGoalItem!!.unit ?: "",
                        onTargetValueChanged = { value ->
                            viewModel.setEditingGoalItem(editingGoalItem!!.copy(targetNumericValue = value))
                        },
                        onCurrentValueChanged = { value ->
                            viewModel.setEditingGoalItem(editingGoalItem!!.copy(currentNumericValue = value))
                        },
                        onUnitChanged = { unit ->
                            viewModel.setEditingGoalItem(editingGoalItem!!.copy(unit = unit))
                        }
                    )
                }

                // 優先度
                PrioritySelector(
                    selectedPriority = editingGoalItem!!.priority,
                    onPriorityChanged = { priority ->
                        viewModel.setEditingGoalItem(editingGoalItem!!.copy(priority = priority))
                    }
                )

                // ご褒美（Celebration）
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editingGoalItem!!.celebration ?: "",
                    onValueChange = { viewModel.setEditingGoalItem(editingGoalItem!!.copy(celebration = it)) },
                    label = { Text("ご褒美") },
                    placeholder = { Text("目標達成時の自分へのご褒美を入力してください") },
                    minLines = 2,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.clearFocus() }
                    )
                )

                // Action Steps
                ActionStepsSection(
                    goalId = editingGoalItem!!.id,
                    viewModel = viewModel
                )

                // 目標の詳細説明
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editingGoalItem!!.detailedDescription ?: "",
                    onValueChange = { viewModel.setEditingGoalItem(editingGoalItem!!.copy(detailedDescription = it)) },
                    label = { Text("目標の詳細説明") },
                    placeholder = { Text("目標の背景や詳細を記入してください") },
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                // 上位目標関連付け
                HigherGoalAssociation(
                    higherGoals = higherGoals,
                    selectedHigherGoalId = editingGoalItem!!.higherGoalId,
                    onSelectHigherGoal = {
                        navController.navigate("higherGoals/${editingGoalItem!!.id}")
                    },
                    onRemoveHigherGoal = {
                        viewModel.setEditingGoalItem(editingGoalItem!!.copy(higherGoalId = null))
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 保存・削除ボタン
                SaveDeleteButtons(
                    goalId = goalId,
                    editingGoalItem = editingGoalItem!!,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }
}

// 目標タイプ選択のトグルタブ
@Composable
fun GoalTypeToggle(
    selectedType: GoalType,
    onTypeChanged: (GoalType) -> Unit
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "目標タイプ *",
                style = MaterialTheme.typography.labelMedium
            )

            Icon(
                Icons.Default.Info,
                contentDescription = "目標タイプの説明",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { showInfoDialog = true }
                    .clip(CircleShape)
                    .padding(2.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 数値目標タブ
            Button(
                onClick = { onTypeChanged(GoalType.NUMERIC) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == GoalType.NUMERIC)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("数値目標")
            }

            // シンプル目標タブ
            Button(
                onClick = { onTypeChanged(GoalType.SIMPLE) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == GoalType.SIMPLE)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("シンプル目標")
            }
        }

        // 目標タイプの説明ダイアログ
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text("目標タイプについて") },
                text = {
                    Column {
                        Text(
                            text = "数値目標",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "具体的な数値で進捗を測定します（売上、体重、読書数など）",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "シンプル目標",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "完了・未完了や取り組み度合いで評価します",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showInfoDialog = false }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

// 数値目標用のフィールド
@Composable
fun NumericGoalFields(
    targetValue: Double,
    currentValue: Double,
    unit: String,
    onTargetValueChanged: (Double) -> Unit,
    onCurrentValueChanged: (Double) -> Unit,
    onUnitChanged: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 目標値
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(2f),
                value = if (targetValue == 0.0) "" else targetValue.toString(),
                onValueChange = {
                    val value = it.toDoubleOrNull() ?: 0.0
                    onTargetValueChanged(value)
                },
                label = { Text("目標値") },
                placeholder = { Text("100") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = unit,
                onValueChange = onUnitChanged,
                label = { Text("単位") },
                placeholder = { Text("万円") }
            )
        }

        // 現在値
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(2f),
                value = if (currentValue == 0.0) "" else currentValue.toString(),
                onValueChange = {
                    val value = it.toDoubleOrNull() ?: 0.0
                    onCurrentValueChanged(value)
                },
                label = { Text("現在値") },
                placeholder = { Text("10") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 優先度選択
@Composable
fun PrioritySelector(
    selectedPriority: GoalPriority,
    onPriorityChanged: (GoalPriority) -> Unit
) {
    Column {
        Text(
            text = "優先度",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GoalPriority.values().forEach { priority ->
                val isSelected = selectedPriority == priority
                Button(
                    onClick = { onPriorityChanged(priority) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = when (priority) {
                            GoalPriority.High -> "High"
                            GoalPriority.Middle -> "Medium"
                            GoalPriority.Low -> "Low"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// 上位目標関連付け
@Composable
fun HigherGoalAssociation(
    higherGoals: List<HigherGoal>,
    selectedHigherGoalId: UUID?,
    onSelectHigherGoal: () -> Unit,
    onRemoveHigherGoal: () -> Unit
) {
    Column {
        Text(
            text = "上位目標関連付け",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val selectedHigherGoal = higherGoals.find { it.id == selectedHigherGoalId }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectHigherGoal() },
            value = selectedHigherGoal?.title ?: "",
            onValueChange = { },
            enabled = false,
            label = { Text("選択された上位目標") },
            placeholder = { Text("上位目標を選択してください") },
            trailingIcon = {
                if (selectedHigherGoalId != null) {
                    IconButton(onClick = onRemoveHigherGoal) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "上位目標を解除"
                        )
                    }
                }
            }
        )
    }
}

// 保存・削除ボタン
@Composable
fun SaveDeleteButtons(
    goalId: UUID?,
    editingGoalItem: GoalItem,
    viewModel: GoalsViewModel,
    navController: NavHostController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                if (goalId == null) {
                    viewModel.addGoalItem(editingGoalItem)
                } else {
                    viewModel.updateGoalItem(editingGoalItem)
                }
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = editingGoalItem.title.isNotBlank()
        ) {
            Text(if (goalId == null) "目標を追加" else "変更を保存")
        }

        // 削除ボタン (編集モード時のみ表示)
        if (goalId != null) {
            var showDeleteDialog by remember { mutableStateOf(false) }

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("目標を削除", color = Color.White)
            }

            // 削除確認ダイアログ
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("目標を削除") },
                    text = { Text("この目標を削除してもよろしいですか？この操作は取り消せません。") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteGoalItem(editingGoalItem)
                                navController.popBackStack()
                            }
                        ) {
                            Text("削除", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("キャンセル")
                        }
                    }
                )
            }
        }
    }
}

// Action Stepsセクション
@Composable
fun ActionStepsSection(
    goalId: UUID,
    viewModel: GoalsViewModel
) {
    val actionSteps by viewModel.getActionStepsForGoal(goalId).collectAsState(initial = emptyList())
    var newStepTitle by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Action Steps",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // 既存のAction Steps
        actionSteps.sortedBy { it.order }.forEach { step ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 丸いチェックマーク
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (step.isCompleted)
                                Color(0xFF4CAF50) // 緑色
                            else
                                Color(0xFFE0E0E0) // グレー
                        )
                        .clickable {
                            viewModel.updateActionStep(step.copy(isCompleted = !step.isCompleted))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (step.isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "完了",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // テキスト（枠なし）
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (step.isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            // テキスト編集機能は後で実装
                        }
                )

                // 削除ボタン
                IconButton(
                    onClick = { viewModel.deleteActionStep(step) }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "ステップを削除",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // アイテムがない場合またはプレースホルダー
        if (actionSteps.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* フォーカスを新規追加フィールドに移す */ },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    // 空の円
                }

                Text(
                    text = "ステップを追加...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 新しいステップを追加するためのフィールド
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "追加",
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(16.dp)
                )
            }

            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = newStepTitle,
                onValueChange = { newStepTitle = it },
                placeholder = { Text("新しいステップを追加...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newStepTitle.isNotBlank()) {
                            val nextOrder = (actionSteps.maxOfOrNull { it.order } ?: -1) + 1
                            viewModel.addActionStep(
                                ActionStep(
                                    goalId = goalId,
                                    title = newStepTitle,
                                    order = nextOrder
                                )
                            )
                            newStepTitle = ""
                        }
                    }
                )
            )
        }
    }
}
