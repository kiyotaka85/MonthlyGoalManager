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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.background
import java.util.UUID
import androidx.compose.material3.Checkbox
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.TextField
import androidx.compose.material3.LinearProgressIndicator

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
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 4

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
                goalType = GoalType.NUMERIC, // すべての月次目標は数値目標
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
                    Column {
                        Text(if (goalId == null) "Add New Goal" else "Edit Goal")
                        Text(
                            text = "Step ${currentStep + 1} of $totalSteps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // プログレスバー
                LinearProgressIndicator(
                    progress = (currentStep + 1).toFloat() / totalSteps,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                // スクロール可能なコンテンツ
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollPosition)
                ) {
                    when (currentStep) {
                        0 -> BasicInfoAndNumericStep(
                            editingGoalItem = editingGoalItem!!,
                            higherGoals = higherGoals,
                            viewModel = viewModel,
                            navController = navController,
                            focusManager = focusManager
                        )
                        1 -> ActionStepsStep(
                            goalId = editingGoalItem!!.id,
                            viewModel = viewModel
                        )
                        2 -> RewardStep(
                            editingGoalItem = editingGoalItem!!,
                            viewModel = viewModel,
                            focusManager = focusManager
                        )
                        3 -> NotesStep(
                            editingGoalItem = editingGoalItem!!,
                            viewModel = viewModel,
                            focusManager = focusManager
                        )
                    }
                }

                // ナビゲーションボタン
                WizardNavigationButtons(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    editingGoalItem = editingGoalItem!!,
                    goalId = goalId,
                    viewModel = viewModel,
                    navController = navController,
                    onPrevious = { currentStep = maxOf(0, currentStep - 1) },
                    onNext = { currentStep = minOf(totalSteps - 1, currentStep + 1) }
                )
            }
        }
    }
}

// 基本情報と数値目標設定ステップ（統合版）
@Composable
fun BasicInfoAndNumericStep(
    editingGoalItem: GoalItem,
    higherGoals: List<HigherGoal>,
    viewModel: GoalsViewModel,
    navController: NavHostController,
    focusManager: FocusManager
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // セクション1: 上位目標関連付け（最初に表示）
        SectionHeader(title = "上位目標との関連付け")
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "月次目標は、具体的で測定可能な数値目標として設定します。定性的な目標（「Java試験に合格する」など）は上位目標として設定してください。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 上位目標関連付け
            HigherGoalAssociation(
                higherGoals = higherGoals,
                selectedHigherGoalId = editingGoalItem.higherGoalId,
                onSelectHigherGoal = {
                    navController.navigate("higherGoals/${editingGoalItem.id}")
                },
                onRemoveHigherGoal = {
                    viewModel.setEditingGoalItem(editingGoalItem.copy(higherGoalId = null))
                }
            )
        }

        // セクション2: 目標の基本情報
        SectionHeader(title = "数値目標の設定")

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 目標タイトル
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = editingGoalItem.title,
                onValueChange = { viewModel.setEditingGoalItem(editingGoalItem.copy(title = it)) },
                label = { Text("目標") },
                placeholder = { Text("例：毎日30分読書する、月に10冊本を読む") },
                minLines = 2,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.clearFocus() }
                )
            )

            // 数値目標の設定
            NumericGoalFields(
                targetValue = editingGoalItem.targetNumericValue ?: 0.0,
                currentValue = editingGoalItem.currentNumericValue ?: 0.0,
                unit = editingGoalItem.unit ?: "",
                onTargetValueChanged = { value ->
                    val updatedGoal = editingGoalItem.copy(targetNumericValue = value)
                    // 進捗率も同時に更新
                    val progress = if (value > 0) {
                        ((editingGoalItem.currentNumericValue ?: 0.0) / value * 100).coerceIn(0.0, 100.0).toInt()
                    } else {
                        0
                    }
                    viewModel.setEditingGoalItem(updatedGoal.copy(currentProgress = progress))
                },
                onCurrentValueChanged = { value ->
                    val updatedGoal = editingGoalItem.copy(currentNumericValue = value)
                    // 進捗率も同時に更新
                    val progress = if ((editingGoalItem.targetNumericValue ?: 0.0) > 0) {
                        (value / (editingGoalItem.targetNumericValue ?: 1.0) * 100).coerceIn(0.0, 100.0).toInt()
                    } else {
                        0
                    }
                    viewModel.setEditingGoalItem(updatedGoal.copy(currentProgress = progress))
                },
                onUnitChanged = { unit ->
                    viewModel.setEditingGoalItem(editingGoalItem.copy(unit = unit))
                }
            )

            // 優先度
            PrioritySelector(
                selectedPriority = editingGoalItem.priority,
                onPriorityChanged = { priority ->
                    viewModel.setEditingGoalItem(editingGoalItem.copy(priority = priority))
                }
            )
        }
    }
}

// アクションステップ
@Composable
fun ActionStepsStep(
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

            TextField(
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

// ご褒美ステップ
@Composable
fun RewardStep(
    editingGoalItem: GoalItem,
    viewModel: GoalsViewModel,
    focusManager: FocusManager
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // セクション4: ご褒美
        SectionHeader(title = "ご褒美")

        Column {
            Text(
                text = "この目標を達成したときのご褒美を記入しましょう",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = editingGoalItem.celebration ?: "",
                onValueChange = { viewModel.setEditingGoalItem(editingGoalItem.copy(celebration = it)) },
                label = { Text("ご褒美") },
                placeholder = { Text("目標達成時の自分へのご褒美を入力してください") },
                minLines = 1,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.clearFocus() }
                )
            )
        }
    }
}

// 備考ステップ
@Composable
fun NotesStep(
    editingGoalItem: GoalItem,
    viewModel: GoalsViewModel,
    focusManager: FocusManager
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // セクション5: 備考
        SectionHeader(title = "備考")

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = editingGoalItem.detailedDescription ?: "",
            onValueChange = { viewModel.setEditingGoalItem(editingGoalItem.copy(detailedDescription = it)) },
            label = { Text("目標の詳細説明") },
            placeholder = { Text("目標の背景や詳細を記入してください") },
            minLines = 3,
            maxLines = 5,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
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
    // 進捗率を自動計算
    val calculateProgress = { current: Double, target: Double ->
        if (target > 0) {
            val progress = (current / target * 100).coerceIn(0.0, 100.0)
            progress.toInt()
        } else {
            0
        }
    }

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

        // 進捗率の表示
        if (targetValue > 0) {
            val progress = calculateProgress(currentValue, targetValue)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "進捗率",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "${progress}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            progress >= 100 -> Color(0xFF4CAF50)
                            progress >= 75 -> MaterialTheme.colorScheme.primary
                            progress >= 50 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                }

                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        progress >= 100 -> Color(0xFF4CAF50)
                        progress >= 75 -> MaterialTheme.colorScheme.primary
                        progress >= 50 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

// 優先度選択（カラーバッジ風）
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
                val (color, textColor) = when (priority) {
                    GoalPriority.High -> if (isSelected)
                        Color(0xFFF44336) to Color.White
                    else
                        Color(0xFFFFEBEE) to Color(0xFFF44336)
                    GoalPriority.Middle -> if (isSelected)
                        Color(0xFF2196F3) to Color.White
                    else
                        Color(0xFFE3F2FD) to Color(0xFF2196F3)
                    GoalPriority.Low -> if (isSelected)
                        Color(0xFF4CAF50) to Color.White
                    else
                        Color(0xFFE8F5E8) to Color(0xFF4CAF50)
                }

                Button(
                    onClick = { onPriorityChanged(priority) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = when (priority) {
                            GoalPriority.High -> "High"
                            GoalPriority.Middle -> "Medium"
                            GoalPriority.Low -> "Low"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// 上位目標関連付け（強化版）
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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectHigherGoal() },
            colors = CardDefaults.cardColors(
                containerColor = if (selectedHigherGoal != null) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (selectedHigherGoal != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedHigherGoal.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (!selectedHigherGoal.description.isNullOrBlank()) {
                                Text(
                                    text = selectedHigherGoal.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onRemoveHigherGoal) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "上位目標を解除",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column {
                        Text(
                            text = "上位目標を選択してください",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "この数値目標がどの上位目標（大きな目的）に貢献するかを選択します",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        if (selectedHigherGoal == null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "💡 上位目標がない場合は、まず「上位目標」画面で定性的な目標を設定してください",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
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

// セクションヘッダー
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

// Wizard用ナビゲーションボタン
@Composable
fun WizardNavigationButtons(
    currentStep: Int,
    totalSteps: Int,
    editingGoalItem: GoalItem,
    goalId: UUID?,
    viewModel: GoalsViewModel,
    navController: NavHostController,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 前のステップへ戻るボタン
        Button(
            onClick = onPrevious,
            enabled = currentStep > 0,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("戻る")
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 次のステップへ進むボタン または 完了ボタン
        Button(
            onClick = {
                if (currentStep == totalSteps - 1) {
                    // 最後のステップの場合は保存して戻る
                    if (goalId == null) {
                        viewModel.addGoalItem(editingGoalItem)
                    } else {
                        viewModel.updateGoalItem(editingGoalItem)
                    }
                    navController.popBackStack()
                } else {
                    // 次のステップへ進む
                    onNext()
                }
            },
            enabled = editingGoalItem.title.isNotBlank(),
            modifier = Modifier.weight(1f)
        ) {
            Text(if (currentStep == totalSteps - 1) "完了" else "次へ")
        }
    }
}
