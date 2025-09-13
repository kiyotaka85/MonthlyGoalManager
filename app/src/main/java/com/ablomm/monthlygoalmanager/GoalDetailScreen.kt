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

    // チェックイン用シート
    var showCheckInSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                        navController = navController,
                        onCheckIn = { showCheckInSheet = true }
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
                        GoalActionStepsContent(
                            goal = goalItem!!,
                            actionSteps = actionStepsState.value,
                            onAdd = { viewModel.addActionStep(it) },
                            onUpdate = { viewModel.updateActionStep(it) },
                            onDelete = { viewModel.deleteActionStep(it) }
                        )
                    }
                }

                // チェックイン履歴セクション
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

                // 削除セクション
                item {
                    GoalDeleteSection(goal = goalItem!!, viewModel = viewModel, navController = navController)
                }
            }
        }
    }

    if (showCheckInSheet && goalItem != null) {
        ModalBottomSheet(
            onDismissRequest = { showCheckInSheet = false },
            sheetState = sheetState
        ) {
            CheckInSheet(
                goalId = goalItem!!.id,
                viewModel = viewModel,
                onClose = { showCheckInSheet = false }
            )
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
fun GoalActionButtons(
    goalId: UUID,
    navController: NavHostController,
    onCheckIn: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onCheckIn,
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
fun GoalActionStepsContent(
    goal: GoalItem,
    actionSteps: List<ActionStep>,
    onAdd: (ActionStep) -> Unit,
    onUpdate: (ActionStep) -> Unit,
    onDelete: (ActionStep) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actionSteps.forEach { step ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(step.title)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val toggled = step.copy(isCompleted = !step.isCompleted)
                    TextButton(onClick = { onUpdate(toggled) }) {
                        Text(if (step.isCompleted) "未完了にする" else "完了")
                    }
                    TextButton(onClick = { onDelete(step) }) {
                        Text("削除")
                    }
                }
            }
        }
        // 追加は簡易的にダミー追加ボタン
        OutlinedButton(onClick = {
            onAdd(
                ActionStep(
                    goalId = goal.id,
                    title = "New step",
                    order = (actionSteps.maxOfOrNull { it.order } ?: 0) + 1
                )
            )
        }) {
            Text("ステップを追加")
        }
    }
}

@Composable
fun GoalDeleteSection(
    goal: GoalItem,
    viewModel: GoalsViewModel,
    navController: NavHostController
) {
    var showConfirm by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("目標の削除", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { showConfirm = true }) { Text("削除する") }
        }
    }
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("削除の確認") },
            text = { Text("この目標を削除しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGoalItem(goal)
                    showConfirm = false
                    navController.popBackStack()
                }) { Text("削除") }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("キャンセル") } }
        )
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
