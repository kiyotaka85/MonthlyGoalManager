package com.ablomm.monthlygoalmanager

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun GoalCard(
    goalItem: GoalItem,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    // コンパクトなシャドウカードデザイン
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("goalDetail/${goalItem.id}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
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

            Spacer(modifier = Modifier.height(4.dp))

            // 2行目：数値情報（右寄せ）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (goalItem.goalType == GoalType.NUMERIC) {
                    // 数値目標の場合: [現在値] / [目標値] [単位] [完了率]
                    val currentValue = goalItem.currentNumericValue?.toInt() ?: 0
                    val targetValue = goalItem.targetNumericValue?.toInt() ?: 1
                    val unit = goalItem.unit ?: ""

                    Text(
                        text = "$currentValue / $targetValue $unit ${goalItem.currentProgress}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // シンプル目標の場合: 完了率のみ
                    Text(
                        text = "${goalItem.currentProgress}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // カード底部の細いプログレスバー
        LinearProgressIndicator(
            progress = goalItem.currentProgress / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = when {
                goalItem.currentProgress >= 100 -> Color(0xFF4CAF50)
                goalItem.currentProgress >= 75 -> MaterialTheme.colorScheme.primary
                goalItem.currentProgress >= 50 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
            
            // 目標カードリスト（8px間隔で配置）
            items(
                items = filteredGoals,
                key = { it.id }
            ) { goalItem ->
                val dismissState = rememberSwipeToDismissBoxState(
                    positionalThreshold = { it * 0.25f },
                    confirmValueChange = { dismissValue ->
                        when (dismissValue) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                navController.navigate("checkIn/${goalItem.id}")
                                false
                            }
                            SwipeToDismissBoxValue.EndToStart -> {
                                navController.navigate("goalEdit/${goalItem.id}")
                                false
                            }
                            else -> false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50).copy(alpha = 0.8f)
                            SwipeToDismissBoxValue.EndToStart -> Color(0xFF2196F3).copy(alpha = 0.8f)
                            else -> Color.Transparent
                        }

                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = color
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentAlignment = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                    else -> Alignment.Center
                                }
                            ) {
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("📊", fontSize = 20.sp)
                                            Text("Check-in", color = Color.White, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("Edit", color = Color.White, fontWeight = FontWeight.Medium)
                                            Text("✏️", fontSize = 20.sp)
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                ) {
                    GoalCard(
                        goalItem = goalItem,
                        navController = navController,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // 最後にスペースを追加
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
