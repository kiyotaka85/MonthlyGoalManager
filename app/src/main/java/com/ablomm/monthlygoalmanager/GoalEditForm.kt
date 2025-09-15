package com.ablomm.monthlygoalmanager

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.HelpOutline // アイコンをインポート
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
import java.util.UUID
import androidx.compose.material3.TextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.Switch
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.height

/**
 * タップするとヒントダイアログを表示するヘルプアイコン
 * @param hintText ダイアログに表示するテキスト
 */
@Composable
fun InfoTooltip(hintText: String) {
    var showDialog by remember { mutableStateOf(false) }

    // IconButtonでクリック領域を確保し、押しやすくする
    IconButton(
        onClick = { showDialog = true },
        modifier = Modifier.size(24.dp) // アイコンサイズより少し大きめに設定
    ) {
        Icon(
            imageVector = Icons.Outlined.HelpOutline, // 丸に「？」のアイコン
            contentDescription = "ヒント",
            tint = MaterialTheme.colorScheme.onSurfaceVariant // アイコンの色
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("ヒント") },
            text = { Text(hintText) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
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
                    isKeyGoal = false,
                    isCompleted = false,
                    displayOrder = 0
                ))
            }
        } else {
            // 編集モード - 【修正点】ViewModelに編集中のデータがないか、
            // もしくは違う目標を編集中だった場合のみ、DBから読み込む
            if (viewModel.editingGoalItem.value?.id != goalId) {
                val loaded = viewModel.getGoalById(goalId)
                viewModel.setEditingGoalItem(loaded)
                // 編集モードの場合は既存の目標を読み込み
            }
        }
        isLoading = false
    }



    val scrollPosition = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (goalId == null) "月次目標の作成" else "目標を編集") },
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
                    viewModel = viewModel,
                    navController = navController,
                    focusManager = focusManager
                )

                // 詳細オプション（折りたたみ式）
                AdvancedOptionsSection(
                    editingGoalItem = editingGoalItem!!,
                    viewModel = viewModel,
                    navController = navController,
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
    viewModel: GoalsViewModel,
    navController: NavHostController,
    focusManager: FocusManager
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 「今月の目標名」入力フィールド
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "今月の目標名",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(4.dp))
                InfoTooltip(hintText = "今月達成したい、主要な目標を一つ設定しましょう。具体的で、行動を促すような名前が効果的です。")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = editingGoalItem.title,
                onValueChange = { viewModel.setEditingGoalItem(editingGoalItem.copy(title = it)) },
                placeholder = { Text("例：単語帳を100ページ進める") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.clearFocus() }
                )
            )
        }

        // 「数値目標」入力フィールド
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "数値目標",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(4.dp))
                InfoTooltip(hintText = "進捗を数値でトラッキングするために必要です。達成率に反映されます。開始値から目標値までの進捗が自動計算されます。")
            }
            Spacer(Modifier.height(8.dp))
            NumericGoalFields(
                targetValue = editingGoalItem.targetNumericValue,
                startValue = editingGoalItem.startNumericValue,
                unit = editingGoalItem.unit,
                isDecimal = editingGoalItem.isDecimal,
                onTargetValueChange = { text ->
                    val value = text.toDoubleOrNull() ?: 0.0
                    val hasDecimal = text.contains('.')
                    val progress = calculateProgress(
                        editingGoalItem.startNumericValue,
                        value,
                        editingGoalItem.currentNumericValue
                    )
                    viewModel.setEditingGoalItem(
                        editingGoalItem.copy(
                            targetNumericValue = value,
                            isDecimal = hasDecimal,
                            currentProgress = progress
                        )
                    )
                },
                onStartValueChange = { text ->
                    val value = text.toDoubleOrNull() ?: 0.0
                    val hasDecimal = text.contains('.')
                    val progress = calculateProgress(
                        value,
                        editingGoalItem.targetNumericValue,
                        value // 開始値と現在値を同期
                    )
                    viewModel.setEditingGoalItem(
                        editingGoalItem.copy(
                            startNumericValue = value,
                            currentNumericValue = value,
                            isDecimal = hasDecimal,
                            currentProgress = progress
                        )
                    )
                },
                onUnitChanged = { unit ->
                    viewModel.setEditingGoalItem(editingGoalItem.copy(unit = unit))
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
                // キー目標設定
                KeyGoalSelector(
                    isKeyGoal = editingGoalItem.isKeyGoal,
                    onKeyGoalChanged = { isKeyGoal ->
                        viewModel.setEditingGoalItem(editingGoalItem.copy(isKeyGoal = isKeyGoal))
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
    startValue: Double,
    unit: String,
    isDecimal: Boolean,
    onTargetValueChange: (String) -> Unit,
    onStartValueChange: (String) -> Unit,
    onUnitChanged: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 開始値
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = if (startValue == 0.0 && !isDecimal) "" else formatNumber(startValue, isDecimal),
            onValueChange = onStartValueChange,
            placeholder = { Text("開始値") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            singleLine = true
        )

        // 目標値
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = if (targetValue == 0.0 && !isDecimal) "" else formatNumber(targetValue, isDecimal),
            onValueChange = onTargetValueChange,
            placeholder = { Text("目標値") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            singleLine = true
        )

        // 単位
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = unit,
            onValueChange = onUnitChanged,
            placeholder = { Text("単位 (例:ページ)") },
            singleLine = true
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

    // アニメーション状態
    var isJustCompleted by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }

    // チェックボックスのスケールアニメーション
    val checkboxScale by animateFloatAsState(
        targetValue = if (isJustCompleted) 1.3f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = {
            if (isJustCompleted) {
                isJustCompleted = false
            }
        },
        label = "checkbox_scale"
    )

    // チェックボックスの背景色アニメーション
    val checkboxColor by animateColorAsState(
        targetValue = if (step.isCompleted) Color(0xFF4CAF50) else Color.Gray,
        animationSpec = tween(durationMillis = 300),
        label = "checkbox_color"
    )

    // テキストの色アニメーション
    val textColor by animateColorAsState(
        targetValue = if (step.isCompleted)
            MaterialTheme.colorScheme.onSurfaceVariant
        else
            MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 300),
        label = "text_color"
    )

    // 完了時のお祝いエフェクト
    LaunchedEffect(step.isCompleted) {
        if (step.isCompleted) {
            isJustCompleted = true
            showCelebration = true
            delay(1500) // 1.5秒後にお祝いエフェクトを非表示
            showCelebration = false
        }
    }

    Box {
        // メインコンテンツ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // アニメーション付きチェックボックス
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .scale(checkboxScale)
                    .background(
                        color = checkboxColor,
                        shape = CircleShape
                    )
                    .clickable {
                        onToggleCompleted()
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
                        color = textColor,
                        textDecoration = if (step.isCompleted)
                            TextDecoration.LineThrough
                        else
                            TextDecoration.None
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

        // お祝いエフェクト（完了時のみ表示）
        if (showCelebration) {
            CelebrationEffect()
        }
    }
}

// お祝いエフェクト用のComposable
@Composable
fun CelebrationEffect() {
    // 複数のパーティクルアニメーション
    val particles = remember { (1..8).map { it } }

    particles.forEach { index ->
        val angle = (index * 45f) // 各パーティクルの角度
        val distance by animateFloatAsState(
            targetValue = 50f,
            animationSpec = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            label = "particle_distance_$index"
        )
        val alpha by animateFloatAsState(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 1200,
                delayMillis = 300
            ),
            label = "particle_alpha_$index"
        )

        // パーティクルの位置計算
        val xOffset = distance * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat()
        val yOffset = distance * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()

        Box(
            modifier = Modifier
                .size(8.dp)
                .offset(x = xOffset.dp, y = yOffset.dp)
                .background(
                    color = when (index % 4) {
                        0 -> Color(0xFFFFD700) // ゴールド
                        1 -> Color(0xFF4CAF50) // グリーン
                        2 -> Color(0xFF2196F3) // ブルー
                        else -> Color(0xFFFF9800) // オレンジ
                    }.copy(alpha = alpha),
                    shape = CircleShape
                )
        ) //これ消さんといてね。
    }

    // 中央の星エフェクト
    val starScale by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "star_scale"
    )

    Text(
        text = "⭐",
        fontSize = 20.sp,
        modifier = Modifier
            .scale(starScale)
            .offset(x = 12.dp, y = (-8).dp)
    )
}

// キー目標選択（美しいSwitch）
@Composable
fun KeyGoalSelector(
    isKeyGoal: Boolean,
    onKeyGoalChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isKeyGoal)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🗝️",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "キー目標に設定する",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isKeyGoal) FontWeight.Bold else FontWeight.Medium
                    )
                }
                Text(
                    text = if (isKeyGoal)
                        "この目標は今月の最重要目標です"
                    else
                        "今月、これさえやれば大丈夫という目標に設定",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Switch(
                checked = isKeyGoal,
                onCheckedChange = onKeyGoalChanged,
                modifier = Modifier.scale(1.1f)
            )
        }
    }
}
