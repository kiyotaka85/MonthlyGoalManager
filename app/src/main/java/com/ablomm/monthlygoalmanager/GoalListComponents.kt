package com.ablomm.monthlygoalmanager

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

// 新しい目標カードデザイ���
@Composable
fun GoalCard(
    goalItem: GoalItem,
    higherGoal: HigherGoal?,
    navController: NavHostController,
    viewModel: GoalsViewModel,
    modifier: Modifier = Modifier
) {
    // このカードに紐づくチェックイン��歴を取得
    val checkIns by viewModel.getCheckInsForGoal(goalItem.id).collectAsState(initial = emptyList())

    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(),
        label = "offset_x_animation"
    )
    val swipeThresholdPx = with(androidx.compose.ui.platform.LocalDensity.current) { 120.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            // .clip(RoundedCornerShape(8.dp)) // Cardがクリップするのでここは不要
    ) {
        // 背景のアクション（編集・チェックイン）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize() // Boxのサイズに合わせる
                .clip(RoundedCornerShape(8.dp)), // 背�����������������������自体をクリップ
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(animatedOffsetX.coerceAtLeast(0f).dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.CenterStart
            ) {
                if(animatedOffsetX > 20.dp.value) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                        Icon(Icons.Default.Edit, "編集", tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("編集", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(abs(animatedOffsetX.coerceAtMost(0f)).dp)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.CenterEnd
            ) {
                if(animatedOffsetX < -20.dp.value) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                        Text("チェックイン", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Check, "チェックイン", tint = Color(0xFF388E3C))
                    }
                }
            }
        }

        // カード本体をCardコンポーザブルで囲むように修正
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = animatedOffsetX.dp)
                .border( // 枠線を追加
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // 少し薄めの色
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(RoundedCornerShape(8.dp)) // 枠線の内側をクリップ
                .background(MaterialTheme.colorScheme.surface) // カードの背景色
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > swipeThresholdPx -> navController.navigate("goalEdit/${goalItem.id}")
                                offsetX < -swipeThresholdPx -> navController.navigate("checkIn/${goalItem.id}")
                            }
                            offsetX = 0f
                        }
                    ) { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(-swipeThresholdPx * 1.5f, swipeThresholdPx * 1.5f)
                    }
                }
                .clickable {
                    if (abs(offsetX) < 20f) navController.navigate("goalDetail/${goalItem.id}")
                }
        ) {
            // カードの中身
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1行目：タイトル（上位目標のアイコンを左に配置）
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 上位目標のアイコン
                    higherGoal?.let {
                        Icon(
                            imageVector = GoalIcons.getIconByName(it.icon),
                            contentDescription = GoalIcons.getIconDescription(it.icon),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 目標タイトル
                    Text(
                        text = goalItem.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 2行目：進捗バ���（現在値吹き出し付き）
                StackedBlockProgressBarWithBubble(
                    goal = goalItem,
                    checkInItems = checkIns
                )
            }

            // キー目標アイコンを右上に配置
            if (goalItem.isKeyGoal) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(
                            color = Color(0xFFF5E6A8), // ベージュに近い黄色
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "キー目標",
                        tint = Color(0xFFB8860B), // より濃い黄色（ダークゴールデンロッド）
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// 新しく追加するカスタム進捗バー
@Composable
fun GoalProgressBarWithCheckIns(
    goal: GoalItem,
    checkInItems: List<CheckInItem>
) {
    val progress = calculateProgressPrecise(
        startValue = goal.startNumericValue,
        targetValue = goal.targetNumericValue,
        currentValue = goal.currentNumericValue
    )
    val progressFraction = (progress / 100f).toFloat().coerceIn(0f, 1f)

    val progressColor = when {
        progress >= 100 -> Color(0xFF4CAF50) // Green
        progress >= 75 -> MaterialTheme.colorScheme.primary
        progress >= 50 -> Color(0xFFFFC107) // Amber
        else -> MaterialTheme.colorScheme.error
    }

    // テーマの色をCanvas外で取得
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp), // 高さを確保
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val yCenter = size.height / 2

            // 1. 背景のトラック
            drawLine(
                color = trackColor,
                start = Offset(0f, yCenter),
                end = Offset(size.width, yCenter),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Butt // 両端を真���直ぐに変更
            )

            // 2. 現在の進捗
            if (progressFraction > 0) {
                drawLine(
                    color = progressColor,
                    start = Offset(0f, yCenter),
                    end = Offset(size.width * progressFraction, yCenter),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // 3. チェックイン地点のドット
            checkInItems.forEach { checkIn ->
                val checkInProgressFraction = (checkIn.progressPercent / 100f).coerceIn(0f, 1f)
                val dotX = size.width * checkInProgressFraction

                // ドットの外枠（少し大きくして目立たせる）
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(dotX, yCenter)
                )
                // ドット本体
                drawCircle(
                    color = progressColor.copy(alpha = 0.8f),
                    radius = 3.dp.toPx(),
                    center = Offset(dotX, yCenter)
                )
            }
        }
    }
}

// 積み��げ式ブロック進捗バー - 革新的な加点法デザイン
@Composable
fun StackedBlockProgressBar(
    goal: GoalItem,
    checkInItems: List<CheckInItem>
) {
    // テーマから色を取得
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val goalLineColor = MaterialTheme.colorScheme.tertiary
    val blockBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) // より濃い枠線色に変更

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp), // 高さを2倍に（16dp → 32dp）
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx() // 進捗バーの太さを2倍に（8dp → 16dp）
            val yCenter = size.height / 2f
            val borderWidth = 2.dp.toPx() // 枠線の太さはそのまま

            // 1. 背景のトラック
            drawLine(
                color = trackColor,
                start = Offset(0f, yCenter),
                end = Offset(size.width, yCenter),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Butt // 両端を真っ直ぐに��更
            )

            // 2. チェックインブロックを積み上げる
            var lastProgressFraction = 0f
            // チェックイン日時でソートして、古いものから順に描画
            val sortedCheckIns = checkInItems.sortedBy { it.checkInDate }

            sortedCheckIns.forEachIndexed { index, checkIn ->
                val currentProgressFraction = (checkIn.progressPercent / 100f).coerceIn(0f, 1f)

                // 前回の進捗からの差分ブロックを描画
                if (currentProgressFraction > lastProgressFraction) {
                    val blockStartX = size.width * lastProgressFraction
                    val blockEndX = size.width * currentProgressFraction
                    val blockWidth = blockEndX - blockStartX

                    // ブロックが十分な幅を持つ場合のみ枠線を描画
                    if (blockWidth > 6.dp.toPx()) { // 閾値を少し上げる
                        // ブロック���とに色を少し変えて、区切りを表現
                        val blockColor = primaryColor.copy(alpha = (0.6f + (index % 5) * 0.08f).coerceIn(0.6f, 1.0f))

                        // ブロック本体を描画
                        drawLine(
                            color = blockColor,
                            start = Offset(blockStartX, yCenter),
                            end = Offset(blockEndX, yCenter),
                            strokeWidth = strokeWidth
                            // capはブロック感を出すためにButt（デフォルト）のまま
                        )

                        // 四角形の枠線を描画（上下左���すべて）
                        val blockTop = yCenter - strokeWidth / 2
                        val blockBottom = yCenter + strokeWidth / 2

                        // 上辺
                        drawLine(
                            color = blockBorderColor,
                            start = Offset(blockStartX, blockTop),
                            end = Offset(blockEndX, blockTop),
                            strokeWidth = borderWidth
                        )

                        // 下辺
                        drawLine(
                            color = blockBorderColor,
                            start = Offset(blockStartX, blockBottom),
                            end = Offset(blockEndX, blockBottom),
                            strokeWidth = borderWidth
                        )

                        // 左辺（最初のブロック以外は重複を避けるため描画���ない）
                        if (index == 0) {
                            drawLine(
                                color = blockBorderColor,
                                start = Offset(blockStartX, blockTop),
                                end = Offset(blockStartX, blockBottom),
                                strokeWidth = borderWidth
                            )
                        }

                        // 右辺
                        drawLine(
                            color = blockBorderColor,
                            start = Offset(blockEndX, blockTop),
                            end = Offset(blockEndX, blockBottom),
                            strokeWidth = borderWidth
                        )
                    } else {
                        // 幅が狭い場合は枠線なしで描画
                        val blockColor = primaryColor.copy(alpha = (0.6f + (index % 5) * 0.08f).coerceIn(0.6f, 1.0f))
                        drawLine(
                            color = blockColor,
                            start = Offset(blockStartX, yCenter),
                            end = Offset(blockEndX, yCenter),
                            strokeWidth = strokeWidth
                        )
                    }
                }
                lastProgressFraction = currentProgressFraction
            }

            // 3. 🎯 目標地点のマーカー
            val goalMarkerX = size.width
            drawLine(
                color = goalLineColor,
                start = Offset(goalMarkerX, yCenter - 12.dp.toPx()), // マーカーも太いバーに合わせて調整
                end = Offset(goalMarkerX, yCenter + 12.dp.toPx()),
                strokeWidth = 3.dp.toPx() // マーカーの線も��し太く
            )
        }
    }
}

// 吹き出し付きの進捗インジケータ。
@Composable
fun GoalProgressIndicatorWithBubble(goal: GoalItem) {
    // 1. start, target, currentの値から精密な進捗率(Double)を計算
    val preciseProgress = calculateProgressPrecise(
        startValue = goal.startNumericValue,
        targetValue = goal.targetNumericValue,
        currentValue = goal.currentNumericValue
    )
    // 2. 進捗率を0.0〜1.0の間のFloatに変換
    val progressFraction = (preciseProgress / 100.0).toFloat().coerceIn(0f, 1f)

    // 3. ���示用の進捗率テキストを生成（小���点以下を四捨五入）
    val progressText = "${preciseProgress.roundToInt()}%"

    // BoxWithConstraintsでコンポーネントの最大幅を取得し���������動的な��置を可能にする
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp) // 吹き出しとバーのための高さを確保
    ) {
        val parentWidthPx = this.maxWidth
        // 吹き出しの幅���定義
        val bubbleWidth = 48.dp
        // 進捗率に基づいて吹き出しのX座標を計算（Dp単位で統一）
        val progressPositionDp = parentWidthPx * progressFraction
        // 吹き出しがコンポーネントの端からはみ出さないようにオ���セットを計算
        val offset = (progressPositionDp - bubbleWidth / 2).coerceIn(0.dp, parentWidthPx - bubbleWidth)

        // 吹き出し��本体と三角形のしっぽ）
        Column(
            modifier = Modifier
                .width(bubbleWidth)
                .offset(x = offset)
                .zIndex(1f), // 吹き出しをバーの前面に表示
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 吹き出��本体 (Cardで影をつけ��)
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = progressText,
                    color = when {
                        preciseProgress >= 100 -> Color(0xFF4CAF50)
                        preciseProgress >= 75 -> MaterialTheme.colorScheme.primary
                        preciseProgress >= 50 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            // 吹き出しのしっぽ（下向きの三角形）
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = when {
                    preciseProgress >= 100 -> Color(0xFF4CAF50)
                    preciseProgress >= 75 -> MaterialTheme.colorScheme.primary
                    preciseProgress >= 50 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                },
                modifier = Modifier
                    .size(20.dp)
                    .offset(y = (-2).dp)
            )
        }

        // ���捗バー
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .align(Alignment.BottomCenter), // Boxの下部に配置
            color = when {
                preciseProgress >= 100 -> Color(0xFF4CAF50)
                preciseProgress >= 75 -> MaterialTheme.colorScheme.primary
                preciseProgress >= 50 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                text = "• カードを左右にスワイプして素早くチェックイン・編集\n• メニューから表示設��でソートやグループ化が可能\n• 目標をタップして詳細を確認",
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
    groupMode: GroupMode = GroupMode.NONE,
    modifier: Modifier = Modifier
) {
    if (filteredGoals.isEmpty()) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp) // ���隔を調整
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
                            viewModel = viewModel, // viewModelを渡す
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
                                viewModel = viewModel,
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
                                    viewModel = viewModel,
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
                                viewModel = viewModel,
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
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 積み上げ式ブロック進捗バー（吹き出し付き）- 革新的な加点法デザイン
@Composable
fun StackedBlockProgressBarWithBubble(
    goal: GoalItem,
    checkInItems: List<CheckInItem>
) {
    val progress = calculateProgressPrecise(
        startValue = goal.startNumericValue,
        targetValue = goal.targetNumericValue,
        currentValue = goal.currentNumericValue
    )
    val progressFraction = (progress / 100f).toFloat().coerceIn(0f, 1f)

    // テーマから色を取得
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val goalLineColor = MaterialTheme.colorScheme.tertiary
    val blockBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp) // スペースを狭く
    ) {
        // 進捗バーと吹き出し部分
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp), // 吹き出しを含む高さ
            contentAlignment = Alignment.BottomStart
        ) {
            val parentWidth = maxWidth

            // 進捗バー部分
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp) // 進捗バーの高さを小さく
                    .align(Alignment.BottomCenter),
                contentAlignment = Alignment.CenterStart
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx() // 少し細く
                    val yCenter = size.height / 2f
                    val borderWidth = 1.5.dp.toPx()

                    // 1. 背景のトラック
                    drawLine(
                        color = trackColor,
                        start = Offset(0f, yCenter),
                        end = Offset(size.width, yCenter),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Butt
                    )

                    // 2. チェックインブロックを積み上げる
                    var lastProgressFraction = 0f
                    val sortedCheckIns = checkInItems.sortedBy { it.checkInDate }

                    sortedCheckIns.forEachIndexed { index, checkIn ->
                        val currentProgressFraction = (checkIn.progressPercent / 100f).coerceIn(0f, 1f)

                        if (currentProgressFraction > lastProgressFraction) {
                            val blockStartX = size.width * lastProgressFraction
                            val blockEndX = size.width * currentProgressFraction
                            val blockWidth = blockEndX - blockStartX

                            if (blockWidth > 4.dp.toPx()) {
                                val blockColor = primaryColor.copy(alpha = (0.6f + (index % 5) * 0.08f).coerceIn(0.6f, 1.0f))

                                // ブロック本体を描画
                                drawLine(
                                    color = blockColor,
                                    start = Offset(blockStartX, yCenter),
                                    end = Offset(blockEndX, yCenter),
                                    strokeWidth = strokeWidth
                                )

                                // 四角形の枠線を描画
                                val blockTop = yCenter - strokeWidth / 2
                                val blockBottom = yCenter + strokeWidth / 2

                                drawLine(color = blockBorderColor, start = Offset(blockStartX, blockTop), end = Offset(blockEndX, blockTop), strokeWidth = borderWidth)
                                drawLine(color = blockBorderColor, start = Offset(blockStartX, blockBottom), end = Offset(blockEndX, blockBottom), strokeWidth = borderWidth)

                                if (index == 0) {
                                    drawLine(color = blockBorderColor, start = Offset(blockStartX, blockTop), end = Offset(blockStartX, blockBottom), strokeWidth = borderWidth)
                                }
                                drawLine(color = blockBorderColor, start = Offset(blockEndX, blockTop), end = Offset(blockEndX, blockBottom), strokeWidth = borderWidth)
                            } else {
                                val blockColor = primaryColor.copy(alpha = (0.6f + (index % 5) * 0.08f).coerceIn(0.6f, 1.0f))
                                drawLine(color = blockColor, start = Offset(blockStartX, yCenter), end = Offset(blockEndX, yCenter), strokeWidth = strokeWidth)
                            }
                        }
                        lastProgressFraction = currentProgressFraction
                    }
                }
            }

            // 現在値の吹き出し（進捗バーの上）
            val bubbleWidth = 48.dp // 幅を小さく調整
            val bubbleX = (parentWidth * progressFraction).coerceIn(bubbleWidth / 2, parentWidth - bubbleWidth / 2)

            Card(
                modifier = Modifier
                    .offset(x = bubbleX - bubbleWidth / 2, y = (-16).dp) // 位置を調整
                    .wrapContentWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Text(
                    text = "${formatNumber(goal.currentNumericValue, goal.isDecimal)}${goal.unit}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }

        // 目標値表示（進捗バー��下の行に独立して配置）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${formatNumber(goal.targetNumericValue, goal.isDecimal)}${goal.unit}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 数値フォーマットのヘルパー関数
private fun formatNumber(value: Double, isDecimal: Boolean): String {
    if (!isDecimal && value % 1.0 == 0.0) {
        return value.toInt().toString()
    }
    // 小数点以下1桁でフォーマット
    return String.format(Locale.getDefault(), "%.1f", value)
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
        (progressInRange / range * 100).coerceAtLeast(0.0)
    } else {
        if (currentValue >= targetValue) 100.0 else 0.0
    }
}
