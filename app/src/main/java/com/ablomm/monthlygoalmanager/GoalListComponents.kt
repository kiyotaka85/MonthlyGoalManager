package com.ablomm.monthlygoalmanager

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlin.math.abs

// UXを最大化するために再設計されたゴールカード
@Composable
fun GoalCard(
    goalItem: GoalItem,
    higherGoal: HigherGoal?,
    navController: NavHostController,
    onCheckIn: (java.util.UUID) -> Unit, // 追加
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(),
        label = "offset_x_animation"
    )
    val swipeThresholdPx = with(androidx.compose.ui.platform.LocalDensity.current) { 120.dp.toPx() }

    val progress = calculateProgressPrecise(
        startValue = goalItem.startNumericValue,
        targetValue = goalItem.targetNumericValue,
        currentValue = goalItem.currentNumericValue
    )
    val progressFraction = ((progress / 100.0).toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "ring_fraction"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        // 背景アクション
        SwipeActionBackground(animatedOffsetX = animatedOffsetX)

        // カード本体
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = animatedOffsetX.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > swipeThresholdPx -> navController.navigate("goalEdit/${goalItem.id}")
                                offsetX < -swipeThresholdPx -> onCheckIn(goalItem.id) // シートを開く
                            }
                            offsetX = 0f
                        }
                    ) { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(-swipeThresholdPx * 1.5f, swipeThresholdPx * 1.5f)
                    }
                }
                .clickable {
                    if (abs(offsetX) < 20f) navController.navigate("goalDetail/${goalItem.id}")
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 上段：左テキスト群 + 右リング
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // タイトル行
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                higherGoal?.let {
                                    Icon(
                                        imageVector = GoalIcons.getIconByName(it.icon),
                                        contentDescription = GoalIcons.getIconDescription(it.icon),
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = goalItem.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // 現在値 / 目標値
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formatNumber(goalItem.currentNumericValue, goalItem.isDecimal),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = " / ${formatNumber(goalItem.targetNumericValue, goalItem.isDecimal)} ${goalItem.unit}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }

                            // 残り or 完了
                            when {
                                progress >= 100.0 -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Celebration,
                                            contentDescription = "Completed",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Completed! Congratulations",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                progress < 70.0 -> {
                                    val remaining = (goalItem.targetNumericValue - goalItem.currentNumericValue).coerceAtLeast(0.0)
                                    Text(
                                        text = "${formatNumber(remaining, goalItem.isDecimal)} ${goalItem.unit} to go",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // リングチャート
                        RingProgress(
                            fraction = animatedFraction,
                            size = 72.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            progressColor = MaterialTheme.colorScheme.primary,
                            label = "${progress.toInt()}%"
                        )
                    }

                    // ボタン行（全幅）
                    FilledTonalButton(
                        onClick = { onCheckIn(goalItem.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Check-in")
                    }
                }

                // キー目標バッジ
                if (goalItem.isKeyGoal) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "キー目標",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RingProgress(
    fraction: Float,
    size: Dp,
    trackColor: Color,
    progressColor: Color,
    label: String
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = this.size.minDimension * 0.12f
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            // トラック
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            // 進捗
            if (fraction > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * fraction,
                    useCenter = false,
                    style = stroke
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SwipeActionBackground(animatedOffsetX: Float) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Edit Action (Left)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(animatedOffsetX.coerceAtLeast(0f).dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.CenterStart
        ) {
            if (animatedOffsetX > 20.dp.value) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 20.dp)
                ) {
                    Icon(Icons.Default.Edit, "編集", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.width(8.dp))
                    Text("編集", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                }
            }
        }
        // Check-in Action (Right)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(abs(animatedOffsetX.coerceAtMost(0f)).dp)
                .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (animatedOffsetX < -20.dp.value) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 20.dp)
                ) {
                    Text("チェックイン", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Check, "チェックイン", tint = Color(0xFF388E3C))
                }
            }
        }
    }
}

// グループヘッダーコンポーネント
@Composable
fun GroupHeader(
    title: String,
    count: Int,
    icon: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon?.let {
                    Icon(
                        imageVector = GoalIcons.getIconByName(it),
                        contentDescription = GoalIcons.getIconDescription(it),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
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

// GoalListContentの引数にviewModelを追加し、GoalCardの呼び出しを修正
@Composable
fun GoalListContent(
    filteredGoals: List<GoalItem>,
    isTipsHidden: Boolean,
    viewModel: GoalsViewModel, // 追加
    navController: NavHostController,
    sortMode: SortMode,
    setSortMode: (SortMode) -> Unit,
    showSortMenu: Boolean,
    setShowSortMenu: (Boolean) -> Unit,
    isHideCompletedGoals: Boolean,
    higherGoals: List<HigherGoal>,
    monthYearText: String,
    context: android.content.Context,
    onCheckIn: (java.util.UUID) -> Unit, // 追加: シート起動
    groupMode: GroupMode = GroupMode.NONE,
    modifier: Modifier = Modifier
) {
    val listBg = Color(0xFFF5F5F5)
    if (filteredGoals.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().background(listBg),
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
            modifier = modifier.fillMaxSize().background(listBg),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // 間隔を調整
        ) {
            if (!isTipsHidden) {
                item {
                    TipsCard(onDismiss = { viewModel.setTipsHidden(true) })
                }
            }

            when (groupMode) {
                GroupMode.NONE -> {
                    items(filteredGoals, key = { it.id.toString() }) { goalItem ->
                        val higherGoal = higherGoals.find { it.id == goalItem.higherGoalId }
                        GoalCard(
                            goalItem = goalItem,
                            higherGoal = higherGoal,
                            navController = navController,
                            onCheckIn = onCheckIn,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                GroupMode.HIGHER_GOAL -> {
                    val groupedGoals = filteredGoals.groupBy { goal ->
                        higherGoals.find { it.id == goal.higherGoalId }
                    }

                    val higherGoalGroups = groupedGoals.filterKeys { it != null }.toList().sortedBy { it.first?.createdAt }
                    val noHigherGoalGroup = groupedGoals[null]

                    higherGoalGroups.forEach { (higherGoal, goals) ->
                        item {
                            GroupHeader(
                                title = higherGoal?.title ?: "上位目標なし",
                                count = goals.size,
                                icon = higherGoal?.icon
                            )
                        }
                        items(goals, key = { it.id.toString() }) { goalItem ->
                            GoalCard(
                                goalItem = goalItem,
                                higherGoal = higherGoal,
                                navController = navController,
                                onCheckIn = onCheckIn,
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
                            )
                        }
                    }

                    noHigherGoalGroup?.let { goals ->
                        if (goals.isNotEmpty()) {
                            item {
                                GroupHeader(title = "上位目標なし", count = goals.size)
                            }
                            items(goals, key = { it.id.toString() }) { goalItem ->
                                GoalCard(
                                    goalItem = goalItem,
                                    higherGoal = null,
                                    navController = navController,
                                    onCheckIn = onCheckIn,
                                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                GroupMode.KEY_GOAL -> {
                    val keyGoals = filteredGoals.filter { it.isKeyGoal }
                    val normalGoals = filteredGoals.filter { !it.isKeyGoal }

                    if (keyGoals.isNotEmpty()) {
                        item {
                            GroupHeader(
                                title = "🗝️ キー目標",
                                count = keyGoals.size
                            )
                        }
                        items(keyGoals, key = { it.id.toString() }) { goalItem ->
                            val higherGoal = higherGoals.find { it.id == goalItem.higherGoalId }
                            GoalCard(
                                goalItem = goalItem,
                                higherGoal = higherGoal,
                                navController = navController,
                                onCheckIn = onCheckIn,
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
                            )
                        }
                    }
                    if (normalGoals.isNotEmpty()) {
                        item {
                            GroupHeader(
                                title = "📋 通常目標",
                                count = normalGoals.size
                            )
                        }
                        items(normalGoals, key = { it.id.toString() }) { goalItem ->
                            val higherGoal = higherGoals.find { it.id == goalItem.higherGoalId }
                            GoalCard(
                                goalItem = goalItem,
                                higherGoal = higherGoal,
                                navController = navController,
                                onCheckIn = onCheckIn,
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalProgressInfo(
    goal: GoalItem
) {
    val progress = calculateProgressPrecise(
        startValue = goal.startNumericValue,
        targetValue = goal.targetNumericValue,
        currentValue = goal.currentNumericValue
    )
    val fraction = ((progress / 100.0).toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "ring_fraction_info"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatNumber(goal.currentNumericValue, goal.isDecimal),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = " / ${formatNumber(goal.targetNumericValue, goal.isDecimal)} ${goal.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
            RingProgress(
                fraction = animatedFraction,
                size = 72.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                progressColor = MaterialTheme.colorScheme.primary,
                label = "${progress.toInt()}%"
            )
        }
    }
}
