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
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {
            // Tips表示
            if (!isTipsHidden) {
                item {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "💡 Tip",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF666666)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Swipe left → Check-in  |  Swipe right → Edit",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF888888),
                                    textAlign = TextAlign.Center
                                )
                            }
                            IconButton(
                                onClick = { viewModel.setTipsHidden(true) }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Hide tips",
                                    tint = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
            }

            // 目標リスト（シンプルなリスト形式）
            items(
                items = filteredGoals,
                key = { it.id }
            ) { goalItem ->
                GoalCard(
                    goalItem = goalItem,
                    navController = navController,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

            }

            // 最後にスペースを追加
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// 共通の進捗表示コンポーネント
@Composable
fun GoalProgressIndicator(goal: GoalItem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. プログレスバー
        LinearProgressIndicator(
            progress = { goal.currentProgress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when {
                goal.currentProgress >= 100 -> Color(0xFF4CAF50)
                goal.currentProgress >= 75 -> MaterialTheme.colorScheme.primary
                goal.currentProgress >= 50 -> Color(0xFFFF9800)
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
                text = "${goal.startNumericValue.toInt()} ${goal.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${goal.targetNumericValue.toInt()} ${goal.unit}",
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
            Text(
                text = "現在: ${goal.currentNumericValue.toInt()} ${goal.unit} (${goal.currentProgress}%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = when {
                    goal.currentProgress >= 100 -> Color(0xFF4CAF50)
                    goal.currentProgress >= 75 -> MaterialTheme.colorScheme.primary
                    goal.currentProgress >= 50 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
            )
        }
    }
}
