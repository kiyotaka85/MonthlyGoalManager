package com.ablomm.monthlygoalmanager

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlin.math.abs
import kotlin.math.ceil

@Composable
fun GoalCard(
    goalItem: GoalItem,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isSwipeInProgress by remember { mutableStateOf(false) }

    // スワイプアニメーション
    val animatedOffsetX by animateFloatAsState(
        targetValue = if (isSwipeInProgress) offsetX else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "swipe_animation"
    )

    // スワイプしきい値
    val swipeThreshold = 120f
    val maxSwipeDistance = 150f

    // カードの実際の高さを計算
    // パディング(12dp * 2) + タイトル(約24dp) + スペース(8dp) + 進捗コンポーネント(約32dp)
    val calculatedCardHeight = 88.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // 背景アクションエリア（カードと完全に同じ高さ）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(calculatedCardHeight)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            // 左側の編集アクション（右スワイプ時に表示）
            if (animatedOffsetX > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width((animatedOffsetX * 2).dp.coerceAtMost(maxSwipeDistance.dp))
                        .background(
                            color = Color(0xFF2196F3),
                            shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "編集",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "編集",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 右側のチェックインアクション（左スワイプ時に表示）
            if (animatedOffsetX < 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width((abs(animatedOffsetX) * 2).dp.coerceAtMost(maxSwipeDistance.dp))
                        .align(Alignment.CenterEnd)
                        .background(
                            color = Color(0xFF4CAF50),
                            shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "チェックイン",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "チェックイン",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // メインカード
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(calculatedCardHeight)
                .offset(x = animatedOffsetX.dp)
                .pointerInput(goalItem.id) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isSwipeInProgress = true
                        },
                        onDragEnd = {
                            try {
                                when {
                                    offsetX > swipeThreshold -> {
                                        // 右スワイプ → 編集
                                        navController.navigate("goalEdit/${goalItem.id}")
                                    }
                                    offsetX < -swipeThreshold -> {
                                        // 左スワイプ → チェックイン
                                        navController.navigate("checkIn/${goalItem.id}")
                                    }
                                }
                            } catch (e: Exception) {
                                // ナビゲーションエラーをキャッチ
                                e.printStackTrace()
                            }
                            offsetX = 0f
                            isSwipeInProgress = false
                        }
                    ) { _, dragAmount ->
                        // スワイプ距離を制限
                        offsetX = (offsetX + dragAmount).coerceIn(-maxSwipeDistance, maxSwipeDistance)
                    }
                }
                .clickable {
                    if (abs(offsetX) < 10f) { // 小さなドラッグはクリックとして扱う
                        try {
                            navController.navigate("goalDetail/${goalItem.id}")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // 1行目：目標名（左寄せ）
                Text(
                    text = goalItem.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 進捗表示コンポーネント
                GoalProgressIndicator(goal = goalItem)
            }
        }
    }
}

@Composable
fun GoalListItem(
    goalItem: GoalItem,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    // シンプルなリストアイテムデザイン
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("goalDetail/${goalItem.id}")
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左側：目標名
        Text(
            text = goalItem.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // 右側：進捗
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 数値目標の表示: [現在値] / [目標値] [単位]
            val currentValue = goalItem.currentNumericValue.toInt()
            val targetValue = goalItem.targetNumericValue.toInt()
            val unit = goalItem.unit

            Text(
                text = "$currentValue / $targetValue $unit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 完了マーク
            if (goalItem.isCompleted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "完了",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun GoalListContent(
    filteredGoals: List<GoalItem>,
    isTipsHidden: Boolean,
    viewModel: GoalsViewModel,
    navController: NavHostController,
    sortMode: SortMode,
    setSortMode: (SortMode) -> Unit,
    showSortMenu: Boolean,
    setShowSortMenu: (Boolean) -> Unit,
    isHideCompletedGoals: Boolean,
    higherGoals: List<HigherGoal>,
    monthYearText: String,
    context: android.content.Context,
    groupMode: GroupMode = GroupMode.NONE,
    modifier: Modifier = Modifier
) {
    if (filteredGoals.isEmpty()) {
        // 空の状態の表示
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🎯",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No goals for this month",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the + button to add a new goal",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ヒントカード
            if (!isTipsHidden) {
                item {
                    TipsCard(onDismiss = { viewModel.setTipsHidden(true) })
                }
            }

            // グループ化の処理
            when (groupMode) {
                GroupMode.NONE -> {
                    // グループ化なし：通常の表示
                    items(filteredGoals, key = { it.id.toString() }) { goalItem ->
                        GoalCard(
                            goalItem = goalItem,
                            navController = navController,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                GroupMode.HIGHER_GOAL -> {
                    // 上位目標でグループ化
                    val groupedGoals = filteredGoals.groupBy { goal ->
                        higherGoals.find { it.id == goal.higherGoalId }
                    }

                    // 上位目標ありのグループを先に表示
                    val higherGoalGroups = groupedGoals.filter { it.key != null }
                    val noHigherGoalGroup = groupedGoals[null]

                    // 上位目標ありのグループを表示
                    higherGoalGroups.forEach { (higherGoal, goals) ->
                        item {
                            GroupHeader(
                                title = higherGoal?.title ?: "上位目標なし",
                                count = goals.size,
                                color = try {
                                    higherGoal?.color?.let { colorString ->
                                        // 色文字列を安全にColorに変換
                                        val colorValue = if (colorString.startsWith("#")) {
                                            colorString.substring(1)
                                        } else {
                                            colorString
                                        }
                                        Color(colorValue.toLong(16) or 0xFF000000)
                                    }
                                } catch (e: Exception) {
                                    null // 変換エラーの場合はnullを返す
                                }
                            )
                        }

                        items(goals, key = { it.id.toString() }) { goalItem ->
                            GoalCard(
                                goalItem = goalItem,
                                navController = navController,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp)
                            )
                        }
                    }

                    // 上位目標なしのグループを最後に表示
                    noHigherGoalGroup?.let { goals ->
                        if (goals.isNotEmpty()) {
                            item {
                                GroupHeader(
                                    title = "上位目標なし",
                                    count = goals.size,
                                    color = null
                                )
                            }

                            items(goals, key = { it.id.toString() }) { goalItem ->
                                GoalCard(
                                    goalItem = goalItem,
                                    navController = navController,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }

                GroupMode.PRIORITY -> {
                    // 優先度でグループ化
                    val groupedGoals = filteredGoals.groupBy { it.priority }
                    val priorityOrder = listOf(GoalPriority.High, GoalPriority.Middle, GoalPriority.Low)

                    priorityOrder.forEach { priority ->
                        val goals = groupedGoals[priority] ?: emptyList()
                        if (goals.isNotEmpty()) {
                            item {
                                GroupHeader(
                                    title = when (priority) {
                                        GoalPriority.High -> "🔴 高優先度"
                                        GoalPriority.Middle -> "🟡 中優先度"
                                        GoalPriority.Low -> "🟢 低優先度"
                                    },
                                    count = goals.size,
                                    color = when (priority) {
                                        GoalPriority.High -> Color(0xFFFF5722)
                                        GoalPriority.Middle -> Color(0xFFFF9800)
                                        GoalPriority.Low -> Color(0xFF4CAF50)
                                    }
                                )
                            }

                            items(goals, key = { it.id.toString() }) { goalItem ->
                                GoalCard(
                                    goalItem = goalItem,
                                    navController = navController,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 精密な進捗率計算のヘルパー関数
private fun calculateProgressPrecise(
    startValue: Double,
    targetValue: Double,
    currentValue: Double
): Double {
    val range = targetValue - startValue
    val progressInRange = currentValue - startValue

    return if (range != 0.0) {
        (progressInRange / range * 100).coerceIn(0.0, 100.0)
    } else {
        if (currentValue >= targetValue) 100.0 else 0.0
    }
}

// 進捗率を小数点一桁まで繰り上がりで表示するヘルパー関数
private fun formatProgressPercentage(progressPercent: Double): String {
    val rounded = kotlin.math.ceil(progressPercent * 10) / 10 // 小数点第二位以下を繰り上がり
    return String.format("%.1f", rounded)
}

// 共通の進捗表示コンポーネント
@Composable
fun GoalProgressIndicator(goal: GoalItem) {
    // 精密な進捗率を計算
    val preciseProgress = calculateProgressPrecise(
        goal.startNumericValue,
        goal.targetNumericValue,
        goal.currentNumericValue
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. プログレスバー
        LinearProgressIndicator(
            progress = { (preciseProgress / 100f).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when {
                preciseProgress >= 100 -> Color(0xFF4CAF50)
                preciseProgress >= 75 -> MaterialTheme.colorScheme.primary
                preciseProgress >= 50 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 2. 開始値と目標値のラベル
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (goal.isDecimal) "${String.format("%.1f", goal.startNumericValue)} ${goal.unit}"
                       else "${goal.startNumericValue.toInt()} ${goal.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (goal.isDecimal) "${String.format("%.1f", goal.targetNumericValue)} ${goal.unit}"
                       else "${goal.targetNumericValue.toInt()} ${goal.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. 現在値と進捗率
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentValueText = if (goal.isDecimal) "${String.format("%.1f", goal.currentNumericValue)} ${goal.unit}"
                                  else "${goal.currentNumericValue.toInt()} ${goal.unit}"

            Text(
                text = "現在: $currentValueText (${formatProgressPercentage(preciseProgress)}%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = when {
                    preciseProgress >= 100 -> Color(0xFF4CAF50)
                    preciseProgress >= 75 -> MaterialTheme.colorScheme.primary
                    preciseProgress >= 50 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
            )
        }
    }
}

// グループヘッダーコンポーネント
@Composable
fun GroupHeader(
    title: String,
    count: Int,
    color: Color? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = color?.copy(alpha = 0.1f) ?: MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color ?: MaterialTheme.colorScheme.onPrimaryContainer
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = color ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ヒントカードコンポーネント
@Composable
fun TipsCard(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "💡",
                        fontSize = 20.sp
                    )
                    Text(
                        text = "使い方のヒント",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "閉じる",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = "• カードを左右にスワイプして素早くチェックイン・編集\n• メニューから表示設定でソートやグループ化が可能\n• 目標をタップして詳細を確認",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
