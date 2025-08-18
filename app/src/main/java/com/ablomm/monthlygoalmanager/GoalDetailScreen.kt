package com.ablomm.monthlygoalmanager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GoalDetailScreen(
    goalId: UUID,
    viewModel: GoalsViewModel,
    navController: NavHostController
) {
    var goalItem by remember { mutableStateOf<GoalItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 各セクションの開閉状態を管理
    var isBasicInfoExpanded by remember { mutableStateOf(false) }
    var isProgressExpanded by remember { mutableStateOf(false) }
    var isActionStepsExpanded by remember { mutableStateOf(false) }
    var isCheckInHistoryExpanded by remember { mutableStateOf(false) }

    val checkInsState = viewModel.getCheckInsForGoal(goalId).collectAsState(initial = emptyList())
    val actionStepsState = viewModel.getActionStepsForGoal(goalId).collectAsState(initial = emptyList())
    val higherGoalsState = viewModel.higherGoalList.collectAsState(initial = emptyList())

    LaunchedEffect(goalId) {
        goalItem = viewModel.getGoalById(goalId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("目標詳細") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("goalEdit/$goalId") }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "編集")
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
        } else if (goalItem != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // アクションボタン
                item {
                    GoalActionButtons(
                        goalId = goalId,
                        navController = navController
                    )
                }

                // 基本情報セクション
                item {
                    ExpandableSection(
                        title = "基本情報",
                        icon = Icons.Default.Info,
                        isExpanded = isBasicInfoExpanded,
                        onToggle = { isBasicInfoExpanded = !isBasicInfoExpanded }
                    ) {
                        GoalBasicInfoContent(
                            goal = goalItem!!,
                            higherGoals = higherGoalsState.value
                        )
                    }
                }

                // 進捗セクション
                item {
                    ExpandableSection(
                        title = "進捗状況",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        isExpanded = isProgressExpanded,
                        onToggle = { isProgressExpanded = !isProgressExpanded }
                    ) {
                        GoalProgressContent(
                            goal = goalItem!!,
                            checkIns = checkInsState.value
                        )
                    }
                }

                // アクションステップセクション（常に表示）
                item {
                    ExpandableSection(
                        title = "アクションステップ",
                        icon = Icons.Default.CheckCircle,
                        isExpanded = isActionStepsExpanded,
                        onToggle = { isActionStepsExpanded = !isActionStepsExpanded }
                    ) {
                        ActionStepsContent(actionSteps = actionStepsState.value, goalId = goalId, viewModel = viewModel)
                    }
                }

                // チェックイン履歴セクション
                if (checkInsState.value.isNotEmpty()) {
                    item {
                        ExpandableSection(
                            title = "チェックイン履歴",
                            icon = Icons.Default.History,
                            isExpanded = isCheckInHistoryExpanded,
                            onToggle = { isCheckInHistoryExpanded = !isCheckInHistoryExpanded }
                        ) {
                            CheckInHistoryContent(checkIns = checkInsState.value)
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("目標が見つかりませんでした")
            }
        }
    }
}

@Composable
fun GoalBasicInfoContent(
    goal: GoalItem,
    higherGoals: List<HigherGoal>
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = goal.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // 数値目標の詳細（常に表示）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("目標値:", fontWeight = FontWeight.Medium)
            Text("${goal.targetNumericValue} ${goal.unit}")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("現在値:", fontWeight = FontWeight.Medium)
            Text("${goal.currentNumericValue} ${goal.unit}")
        }

        // キー目標
        if (goal.isKeyGoal) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🗝️", fontSize = 20.sp)
                Text("キー目標", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        // 上位目標
        val higherGoal = higherGoals.find { it.id == goal.higherGoalId }
        if (higherGoal != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("上位目標:", fontWeight = FontWeight.Medium)
                Text(higherGoal.title)
            }
        }

        // ご褒美
        if (!goal.celebration.isNullOrBlank()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🎁 ご褒美:", fontWeight = FontWeight.Medium)
                Text(
                    text = goal.celebration,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 詳細説明
        if (!goal.detailedDescription.isNullOrBlank()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("詳細説明:", fontWeight = FontWeight.Medium)
                Text(
                    text = goal.detailedDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GoalProgressContent(
    goal: GoalItem,
    checkIns: List<CheckInItem>
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Premium bubble progress indicator
        GoalProgressIndicatorWithBubble(goal = goal)

        if (goal.isCompleted) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700)
                )
                Text(
                    text = "完了済み",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
        }

        // 統計情報
        if (checkIns.isNotEmpty()) {
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${checkIns.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "チェックイン回数",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val avgProgress = checkIns.map { it.progressPercent }.average().toInt()
                    Text(
                        text = "${avgProgress}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "平均進捗",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun ActionStepsContent(
    actionSteps: List<ActionStep>,
    goalId: UUID,
    viewModel: GoalsViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStep by remember { mutableStateOf<ActionStep?>(null) }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 追加ボタン
        OutlinedButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("アクションステップを追加")
        }

        // アクションステップリスト
        actionSteps.sortedBy { it.order }.forEach { step ->
            ActionStepItem(
                step = step,
                onToggleComplete = {
                    viewModel.updateActionStep(step.copy(isCompleted = !step.isCompleted))
                },
                onEdit = { editingStep = step },
                onDelete = { viewModel.deleteActionStep(step) }
            )
        }

        // 進捗表示
        if (actionSteps.isNotEmpty()) {
            val completedSteps = actionSteps.count { it.isCompleted }
            val totalSteps = actionSteps.size

            HorizontalDivider()
            Text(
                text = "完了: $completedSteps / $totalSteps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "まだアクションステップがありません",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }

    // 追加ダイアログ
    if (showAddDialog) {
        ActionStepDialog(
            actionStep = null,
            onDismiss = { showAddDialog = false },
            onSave = { title ->
                val newStep = ActionStep(
                    goalId = goalId,
                    title = title,
                    order = actionSteps.size
                )
                viewModel.addActionStep(newStep)
                showAddDialog = false
            }
        )
    }

    // 編集ダイアログ
    editingStep?.let { step ->
        ActionStepDialog(
            actionStep = step,
            onDismiss = { editingStep = null },
            onSave = { title ->
                viewModel.updateActionStep(step.copy(title = title))
                editingStep = null
            }
        )
    }
}

@Composable
fun ActionStepItem(
    step: ActionStep,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (step.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 完了チェックボタン
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (step.isCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                    contentDescription = if (step.isCompleted) "完了済み" else "完了にする",
                    tint = if (step.isCompleted) Color(0xFF4CAF50) else Color.Gray
                )
            }

            // ステップタイトル
            Text(
                text = step.title,
                modifier = Modifier.weight(1f),
                style = if (step.isCompleted) {
                    MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                } else {
                    MaterialTheme.typography.bodyMedium
                }
            )

            // 編集ボタン
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "編集",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // 削除ボタン
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "削除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ActionStepDialog(
    actionStep: ActionStep?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var title by remember { mutableStateOf(actionStep?.title ?: "") }
    val isEditing = actionStep != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditing) "アクションステップを編集" else "新しいアクションステップ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("タイトル") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("キャンセル")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(title.trim())
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text(if (isEditing) "更新" else "追加")
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CheckInHistoryContent(checkIns: List<CheckInItem>) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        checkIns.take(5).forEach { checkIn ->
            val dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(checkIn.checkInDate),
                ZoneId.systemDefault()
            )
            val formattedDate = dateTime.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${formatProgressPercentageFromInt(checkIn.progressPercent)}%",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    if (checkIn.comment.isNotBlank()) {
                        Text(
                            text = checkIn.comment,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        if (checkIns.size > 5) {
            Text(
                text = "他 ${checkIns.size - 5} 件のチェックイン",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GoalActionButtons(
    goalId: UUID,
    navController: NavHostController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { navController.navigate("checkIn/$goalId") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("チェックイン")
        }

//        OutlinedButton(
//            onClick = { navController.navigate("goalEdit/$goalId") },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("編集")
//        }
    }
}

// 整数の進捗率を小数点一桁まで繰り上がりで表示するヘルパー関数
// 注意：この関数は整数値しか受け取らないため、精密な値は失われます
// 可能な場合は、元の目標データから精密計算を行うことを推奨
private fun formatProgressPercentageFromInt(progressPercent: Int): String {
    val progressDouble = progressPercent.toDouble()
    return java.lang.String.format(java.util.Locale.getDefault(), "%.1f", progressDouble)
}

@Composable
fun ExpandableSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { onToggle() }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(icon, contentDescription = null)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (isExpanded) {
                content()
            }
        }
    }
}
