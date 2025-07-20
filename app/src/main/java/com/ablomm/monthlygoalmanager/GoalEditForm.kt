package com.ablomm.monthlygoalmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.material3.TextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.Switch
import androidx.compose.ui.text.input.KeyboardCapitalization

// 数値フォーマットのヘルパー関数
private fun formatNumber(value: Double, isDecimal: Boolean): String {
    if (!isDecimal && value % 1.0 == 0.0) {
        return value.toInt().toString()
    }
    return value.toString()
}

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
    var showAdvancedOptions by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // 上位目標のリストを取得
    val higherGoals by viewModel.higherGoalList.collectAsState(initial = emptyList())

    // 初期化処理（goalIdに基づいて一度だけ実行）
    LaunchedEffect(key1 = goalId) {
        if (goalId == null) {
            // 新規作成モード - ViewModelの状態をチェックして初期化が必要かを判断
            val currentValue = viewModel.editingGoalItem.value
            if (currentValue == null || currentValue.title.isEmpty()) {
                viewModel.setEditingGoalItem(GoalItem(
                    id = UUID.randomUUID(),
                    title = "",
                    detailedDescription = "",
                    targetMonth = targetMonth ?: 2025007,
                    targetNumericValue = 0.0,
                    startNumericValue = 0.0,
                    currentNumericValue = 0.0,
                    unit = "",
                    currentProgress = 0,
                    priority = GoalPriority.Middle,
                    isCompleted = false,
                    displayOrder = 0
                ))
            }
        } else {
            // 編集モード
            val loaded = viewModel.getGoalById(goalId)
            viewModel.setEditingGoalItem(loaded)
            // 編集モードで上位目標が設定されている場合は詳細オプションを展開
            if (loaded?.higherGoalId != null) {
                showAdvancedOptions = true
            }
        }
        isLoading = false
    }

    // 上位目標選択結果の受け取り
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.savedStateHandle?.get<String>("selected_higher_goal_id")?.let { selectedId ->
            val higherGoalId = UUID.fromString(selectedId)
            // 現在の編集中のGoalItemを保持したまま上位目標IDのみ更新
            viewModel.editingGoalItem.value?.let { currentGoal ->
                viewModel.setEditingGoalItem(currentGoal.copy(higherGoalId = higherGoalId))
            }
            // 上位目標を選択した後は詳細オプションを開いておく
            showAdvancedOptions = true
            // 使用済みのデータをクリア
            navBackStackEntry?.savedStateHandle?.remove<String>("selected_higher_goal_id")
        }
    }

    // 上位目標が設定されている場合は自動的に詳細オプションを展開
    LaunchedEffect(editingGoalItem?.higherGoalId) {
        if (editingGoalItem?.higherGoalId != null) {
            showAdvancedOptions = true
        }
    }

    val scrollPosition = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (goalId == null) "新しい目標" else "目標を編集") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
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
                    .verticalScroll(scrollPosition)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 必須項目セクション
                RequiredFieldsSection(
                    editingGoalItem = editingGoalItem!!,
                    higherGoals = higherGoals,
                    viewModel = viewModel,
                    navController = navController,
                    focusManager = focusManager
                )

                // 詳細オプション（折りたたみ式）
                AdvancedOptionsSection(
                    editingGoalItem = editingGoalItem!!,
                    viewModel = viewModel,
                    navController = navController,
                    higherGoals = higherGoals,
                    showAdvancedOptions = showAdvancedOptions,
                    onToggleAdvancedOptions = { showAdvancedOptions = !showAdvancedOptions },
                    focusManager = focusManager
                )

                // 保存ボタン
                SaveButton(
                    editingGoalItem = editingGoalItem!!,
                    goalId = goalId,
                    viewModel = viewModel,
                    onSuccess = { showSuccessDialog = true }
                )

                // 削除ボタン（編集モード時のみ）
                if (goalId != null) {
                    DeleteButton(
                        editingGoalItem = editingGoalItem!!,
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }
    }

    // 成功ダイアログ
    if (showSuccessDialog) {
        GoalCreatedSuccessDialog(
            goalTitle = editingGoalItem?.title ?: "",
            onGoHome = {
                showSuccessDialog = false
                navController.popBackStack()
            }
        )
    }
}

// 必須項目セクション
@Composable
fun RequiredFieldsSection(
    editingGoalItem: GoalItem,
    higherGoals: List<HigherGoal>,
    viewModel: GoalsViewModel,
    navController: NavHostController,
    focusManager: FocusManager
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // セクション1: 目標の基本情報
        SectionHeader(title = "目標の基本情報")

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 目標
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = editingGoalItem.title,
                onValueChange = { viewModel.setEditingGoalItem(editingGoalItem.copy(title = it)) },
                label = { Text("目標") },
                placeholder = { Text("例：体重を70kgまで減らす") },
                minLines = 3,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.clearFocus() }
                )
            )

            // 数値設定（必須項目）
            NumericGoalFields(
                targetValue = editingGoalItem.targetNumericValue ?: 0.0,
                currentValue = editingGoalItem.currentNumericValue ?: 0.0,
                startValue = editingGoalItem.startNumericValue ?: 0.0,
                unit = editingGoalItem.unit ?: "",
                isDecimal = editingGoalItem.isDecimal,
                onTargetValueChanged = { value ->
                    val updatedGoal = editingGoalItem.copy(targetNumericValue = value)
                    // 新しい進捗率計算ロジックを使用
                    val progress = calculateProgress(
                        editingGoalItem.startNumericValue,
                        value,
                        editingGoalItem.currentNumericValue
                    )
                    viewModel.setEditingGoalItem(updatedGoal.copy(currentProgress = progress))
                },
                onCurrentValueChanged = { value ->
                    val updatedGoal = editingGoalItem.copy(currentNumericValue = value)
                    // 新しい進捗率計算ロジックを使用
                    val progress = calculateProgress(
                        editingGoalItem.startNumericValue,
                        editingGoalItem.targetNumericValue,
                        value
                    )
                    viewModel.setEditingGoalItem(updatedGoal.copy(currentProgress = progress))
                },
                onStartValueChanged = { value ->
                    // 開始値変更時は現在値も同じ値に設定し、進捗率を再計算
                    val updatedGoal = editingGoalItem.copy(
                        startNumericValue = value,
                        currentNumericValue = value // 現在値も開始値と同じに設定
                    )
                    val progress = calculateProgress(
                        value, // 新しい開始値を使用
                        updatedGoal.targetNumericValue, // 更新されたGoalから値を取得
                        value // 新しい現在値（開始値と同じ）を使用
                    )
                    viewModel.setEditingGoalItem(updatedGoal.copy(currentProgress = progress))
                },
                onUnitChanged = { unit ->
                    viewModel.setEditingGoalItem(editingGoalItem.copy(unit = unit))
                },
                onIsDecimalChanged = { isDecimal ->
                    viewModel.setEditingGoalItem(editingGoalItem.copy(isDecimal = isDecimal))
                }
            )
        }
    }
}

// 詳細オプションセクション
@Composable
fun AdvancedOptionsSection(
    editingGoalItem: GoalItem,
    viewModel: GoalsViewModel,
    navController: NavHostController,
    higherGoals: List<HigherGoal>,
    showAdvancedOptions: Boolean,
    onToggleAdvancedOptions: () -> Unit,
    focusManager: FocusManager
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 詳細オプションのトグルヘッダー
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleAdvancedOptions() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "詳細オプション",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Icon(
                    imageVector = if (showAdvancedOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (showAdvancedOptions) "詳細オプションを閉じる" else "詳細オプションを開く",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 詳細オプションの内容
        if (showAdvancedOptions) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 上位目標関連付け（最優先）
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

                // 優先度
                PrioritySelector(
                    selectedPriority = editingGoalItem.priority,
                    onPriorityChanged = { priority ->
                        viewModel.setEditingGoalItem(editingGoalItem.copy(priority = priority))
                    }
                )

                // アクションステップ
                ActionStepsSection(
                    goalId = editingGoalItem.id,
                    viewModel = viewModel
                )

                // ご褒美
                Text(
                    text = "目標達成時のご褒美",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editingGoalItem.celebration ?: "",
                    onValueChange = { viewModel.setEditingGoalItem(editingGoalItem.copy(celebration = it)) },
                    placeholder = { Text("目標達成時の自分へのご褒美を入力してください") },
                    minLines = 1,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.clearFocus() }
                    )
                )

                // 備考

                Text(
                    text = "備考",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editingGoalItem.detailedDescription ?: "",
                    onValueChange = { viewModel.setEditingGoalItem(editingGoalItem.copy(detailedDescription = it)) },
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
    }
}

// 数値目標用のフィールド
@Composable
fun NumericGoalFields(
    targetValue: Double,
    currentValue: Double,
    startValue: Double,
    unit: String,
    isDecimal: Boolean,
    onTargetValueChanged: (Double) -> Unit,
    onCurrentValueChanged: (Double) -> Unit,
    onStartValueChanged: (Double) -> Unit,
    onUnitChanged: (String) -> Unit,
    onIsDecimalChanged: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 小数点切り替えスイッチ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "小数点以下の値を入力する",
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = isDecimal,
                onCheckedChange = onIsDecimalChanged
            )
        }

        // 目標値と単位（同じ行）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(2f),
                value = if (targetValue == 0.0) "" else formatNumber(targetValue, isDecimal),
                onValueChange = {
                    val value = it.toDoubleOrNull() ?: 0.0
                    onTargetValueChanged(value)
                },
                label = { Text("目標値") },
                placeholder = { Text("100") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
                )
            )

            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = unit,
                onValueChange = onUnitChanged,
                label = { Text("単位") },
                placeholder = { Text("万円") }
            )
        }

        // 開始値（単独行）
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = if (startValue == 0.0) "" else formatNumber(startValue, isDecimal),
            onValueChange = {
                val value = it.toDoubleOrNull() ?: 0.0
                onStartValueChanged(value)
            },
            label = { Text("開始値") },
            placeholder = { Text("0") },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
            ),
            supportingText = { Text("目標開始時点の数値") }
        )

        // 使用例の表示
        if (startValue != 0.0 && targetValue > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "例：${formatNumber(startValue, isDecimal)}${unit} → ${formatNumber(targetValue, isDecimal)}${unit}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "目標追加時の現在値は開始値と同じ値になります",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

// 保存ボタン
@Composable
fun SaveButton(
    editingGoalItem: GoalItem,
    goalId: UUID?,
    viewModel: GoalsViewModel,
    onSuccess: () -> Unit
) {
    Button(
        onClick = {
            if (goalId == null) {
                viewModel.addGoalItem(editingGoalItem)
            } else {
                viewModel.updateGoalItem(editingGoalItem)
            }
            onSuccess()
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = editingGoalItem.title.isNotBlank()
    ) {
        Text(if (goalId == null) "目標を追加" else "変更を保存")
    }
}

// 削除ボタン
@Composable
fun DeleteButton(
    editingGoalItem: GoalItem,
    viewModel: GoalsViewModel,
    navController: NavHostController
) {
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

// 成功ダイアログ
@Composable
fun GoalCreatedSuccessDialog(
    goalTitle: String,
    onGoHome: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onGoHome() },
        title = { Text("目標を作成しました") },
        text = { Text("目標「$goalTitle」が正常に作成されました。") },
        confirmButton = {
            TextButton(
                onClick = onGoHome
            ) {
                Text("ホームに戻る")
            }
        }
    )
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

// アクションステップセクション
@Composable
fun ActionStepsSection(
    goalId: UUID,
    viewModel: GoalsViewModel
) {
    val actionSteps by viewModel.getActionStepsForGoal(goalId).collectAsState(initial = emptyList())
    var newStepText by remember { mutableStateOf("") }
    var showAddField by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "アクションステップ",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 既存のアクションステップリスト
        if (actionSteps.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                actionSteps.forEach { step ->
                    ActionStepItem(
                        step = step,
                        onToggleCompleted = {
                            viewModel.updateActionStep(step.copy(isCompleted = !step.isCompleted))
                        },
                        onDeleteStep = {
                            viewModel.deleteActionStep(step)
                        },
                        onUpdateText = { newText ->
                            viewModel.updateActionStep(step.copy(title = newText))
                        }
                    )
                }
            }
        }

        // 新しいステップ追加UI
        if (showAddField) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = newStepText,
                    onValueChange = { newStepText = it },
                    placeholder = { Text("新しいステップを入力...") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newStepText.isNotBlank()) {
                                val newStep = ActionStep(
                                    id = UUID.randomUUID(),
                                    goalId = goalId,
                                    title = newStepText,
                                    isCompleted = false,
                                    order = actionSteps.size
                                )
                                viewModel.addActionStep(newStep)
                                newStepText = ""
                                showAddField = false
                            }
                        }
                    )
                )

                IconButton(
                    onClick = {
                        if (newStepText.isNotBlank()) {
                            val newStep = ActionStep(
                                id = UUID.randomUUID(),
                                goalId = goalId,
                                title = newStepText,
                                isCompleted = false,
                                order = actionSteps.size
                            )
                            viewModel.addActionStep(newStep)
                            newStepText = ""
                            showAddField = false
                        }
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "追加")
                }

                IconButton(
                    onClick = {
                        showAddField = false
                        newStepText = ""
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "キャンセル")
                }
            }
        } else {
            // ステップ追加ボタン
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAddField = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "追加",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (actionSteps.isEmpty()) "ステップを追加..." else "新しいステップを追加",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// 個別のアクションステップアイテム
@Composable
fun ActionStepItem(
    step: ActionStep,
    onToggleCompleted: () -> Unit,
    onDeleteStep: () -> Unit,
    onUpdateText: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(step.title) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // チェックマーク
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = if (step.isCompleted) Color(0xFF4CAF50) else Color.Gray,
                    shape = CircleShape
                )
                .clickable { onToggleCompleted() },
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

        // テキスト部分
        if (isEditing) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = editText,
                    onValueChange = { editText = it },
                    label = { Text("ステップ内容") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (editText.isNotBlank()) {
                                onUpdateText(editText)
                                isEditing = false
                            }
                        }
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (editText.isNotBlank()) {
                            onUpdateText(editText)
                            isEditing = false
                        }
                    },
                    enabled = editText.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "保存",
                        tint = if (editText.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        editText = step.title
                        isEditing = false
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "キャンセル")
                }
            }
        } else {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = step.title,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            isEditing = true
                            editText = step.title
                        }
                        .padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (step.isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (step.isCompleted)
                        androidx.compose.ui.text.style.TextDecoration.LineThrough
                    else
                        androidx.compose.ui.text.style.TextDecoration.None
                )

                // 削除ボタン（編集モードでない時のみ表示）
                IconButton(
                    onClick = onDeleteStep,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "削除",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
