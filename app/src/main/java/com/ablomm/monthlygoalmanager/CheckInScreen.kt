package com.ablomm.monthlygoalmanager

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CheckInScreen(
    goalId: UUID,
    viewModel: GoalsViewModel,
    navController: NavHostController
) {
    var goalItemState by remember { mutableStateOf<GoalItem?>(null) }
    var progressPercent by remember { mutableStateOf("") }
    var numericValue by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    var savedCheckIn by remember { mutableStateOf<CheckInItem?>(null) }

    // 変更量計算用の状態変数を追加
    var changeAmount by remember { mutableStateOf(0.0) }
    var previousValue by remember { mutableStateOf(0.0) }
    var progressIncrease by remember { mutableStateOf(0) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val checkInsState = viewModel.getCheckInsForGoal(goalId).collectAsState(initial = emptyList())

    LaunchedEffect(goalId) {
        goalItemState = viewModel.getGoalById(goalId)
        progressPercent = goalItemState?.currentProgress?.toString() ?: "0"
        numericValue = goalItemState?.currentNumericValue?.toString() ?: ""
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check-in") },
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
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Goal Information Card
                goalItemState?.let { goal ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // 共通の進捗表示コンポーネントを使用
                            GoalProgressIndicator(goal = goal)
                        }
                    }
                }

                // New Check-in Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "New Check-in",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        goalItemState?.let { goal ->
                            // 数値目標：単位付きの数値入力
                            OutlinedTextField(
                                value = numericValue,
                                onValueChange = { text ->
                                    val value = text.toDoubleOrNull()
                                    if (value == null && text.isNotEmpty()) return@OutlinedTextField
                                    if (value != null && value < 0) return@OutlinedTextField
                                    numericValue = text
                                },
                                label = { Text("現在の数値 (${goal.unit})") },
                                placeholder = { Text("例：${goal.targetNumericValue.toInt()}") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            label = { Text("Comments") },
                            placeholder = { Text("What did you accomplish? How do you feel?") },
                            minLines = 3,
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                goalItemState?.let { goal ->
                                    val currentValue = numericValue.toDoubleOrNull() ?: 0.0

                                    // 変更量と進捗率の増加を計算
                                    previousValue = goal.currentNumericValue
                                    changeAmount = currentValue - previousValue

                                    val oldProgress = goal.currentProgress
                                    val newProgress = calculateProgress(
                                        goal.startNumericValue,
                                        goal.targetNumericValue,
                                        currentValue
                                    )
                                    progressIncrease = newProgress - oldProgress

                                    val checkIn = CheckInItem(
                                        goalId = goalId,
                                        progressPercent = newProgress,
                                        comment = comment.trim()
                                    )
                                    viewModel.addCheckIn(checkIn)

                                    // Update goal progress
                                    val updatedGoal = goal.copy(
                                        currentNumericValue = currentValue,
                                        currentProgress = newProgress,
                                        isCompleted = newProgress >= 100
                                    )
                                    viewModel.updateGoalItem(updatedGoal)

                                    savedCheckIn = checkIn
                                    showCompletionDialog = true
                                }
                            },
                            enabled = goalItemState?.let {
                                numericValue.isNotBlank()
                            } ?: false,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Check-in")
                        }
                    }
                }

                // Check-in History
                if (checkInsState.value.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Check-in History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            LazyColumn(
                                modifier = Modifier.heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(checkInsState.value) { checkIn ->
                                    CheckInHistoryItem(checkIn = checkIn)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Check-in Completion Dialog
    if (showCompletionDialog && savedCheckIn != null && goalItemState != null) {
        CheckInCompletionDialog(
            goal = goalItemState!!,
            checkIn = savedCheckIn!!,
            changeAmount = changeAmount,
            previousValue = previousValue,
            progressIncrease = progressIncrease,
            onShare = { shareText ->
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "進捗を共有"))
                showCompletionDialog = false
                navController.popBackStack()
            },
            onDismiss = {
                showCompletionDialog = false
                navController.popBackStack()
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CheckInHistoryItem(checkIn: CheckInItem) {
    val dateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(checkIn.checkInDate),
        ZoneId.systemDefault()
    )
    val formattedDate = dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${checkIn.progressPercent}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            if (checkIn.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = checkIn.comment,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// 数値フォーマットのヘルパー関数
private fun formatNumber(value: Double, isDecimal: Boolean): String {
    if (!isDecimal && value % 1.0 == 0.0) {
        return value.toInt().toString()
    }
    return String.format("%.1f", value)
}

// 紙吹雪の個別要素データクラス
data class ConfettiPiece(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val color: Color,
    val size: Float,
    val rotation: Float
)

// 紙吹雪アニメーションコンポーネント
@Composable
fun ConfettiAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val confettiCount = 80

    // 明るく、ゴージャスな紙吹雪の色リスト
    val colors = listOf(
        Color(0xFFFFD700), // Bright Gold
        Color(0xFFFFA500), // Bright Orange
        Color(0xFFFF1493), // Deep Pink
        Color(0xFF00FFFF), // Cyan
        Color(0xFFFF69B4), // Hot Pink
        Color(0xFFFFFF00), // Bright Yellow
        Color(0xFF32CD32), // Lime Green
        Color(0xFFFF4500), // Orange Red
        Color(0xFFDA70D6), // Orchid
        Color(0xFF00FA9A), // Medium Spring Green
        Color(0xFFFF6347), // Tomato
        Color(0xFF9370DB), // Medium Purple
        Color(0xFFFFB6C1), // Light Pink
        Color(0xFF98FB98), // Pale Green
        Color(0xFFFFFFE0), // Light Yellow
        Color(0xFFFFC0CB)  // Pink
    )

    // 紙吹雪のピースを生成
    val confettiPieces = remember {
        List(confettiCount) { index ->
            ConfettiPiece(
                id = index,
                startX = Random.nextFloat() * 1000f,
                startY = -Random.nextFloat() * 200f,
                color = colors.random(),
                size = Random.nextFloat() * 8f + 6f,
                rotation = Random.nextFloat() * 360f
            )
        }
    }

    // アニメーション値
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        confettiPieces.forEach { piece ->
            val progress = (animationProgress + piece.id * 0.08f) % 1f
            val x = piece.startX + sin(progress * 8f) * 40f
            val y = piece.startY + progress * (size.height + 200f)
            val alpha = (1f - progress * 0.7f).coerceIn(0.3f, 1f)

            // 影効果
            drawCircle(
                color = Color.Black.copy(alpha = alpha * 0.3f),
                radius = piece.size + 1f,
                center = Offset(x + 2f, y + 2f)
            )

            // メインの紙吹雪
            drawCircle(
                color = piece.color.copy(alpha = alpha),
                radius = piece.size,
                center = Offset(x, y)
            )

            // 白い縁取り
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.8f),
                radius = piece.size,
                center = Offset(x, y),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // 光沢効果
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.6f),
                radius = piece.size * 0.3f,
                center = Offset(x - piece.size * 0.3f, y - piece.size * 0.3f)
            )

            // 追加の装飾的な形状
            when (piece.id % 4) {
                0 -> {
                    // 星型風
                    drawCircle(
                        color = piece.color.copy(alpha = alpha * 0.8f),
                        radius = piece.size * 0.6f,
                        center = Offset(x, y)
                    )
                }
                1 -> {
                    // ダイヤモンド型
                    drawRect(
                        color = piece.color.copy(alpha = alpha),
                        topLeft = Offset(x - piece.size * 0.7f, y - piece.size * 0.7f),
                        size = androidx.compose.ui.geometry.Size(piece.size * 1.4f, piece.size * 1.4f)
                    )
                    drawRect(
                        color = Color.White.copy(alpha = alpha * 0.8f),
                        topLeft = Offset(x - piece.size * 0.7f, y - piece.size * 0.7f),
                        size = androidx.compose.ui.geometry.Size(piece.size * 1.4f, piece.size * 1.4f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                    )
                }
                2 -> {
                    // ハート型風
                    drawCircle(
                        color = piece.color.copy(alpha = alpha),
                        radius = piece.size * 0.5f,
                        center = Offset(x - piece.size * 0.3f, y - piece.size * 0.2f)
                    )
                    drawCircle(
                        color = piece.color.copy(alpha = alpha),
                        radius = piece.size * 0.5f,
                        center = Offset(x + piece.size * 0.3f, y - piece.size * 0.2f)
                    )
                }
                else -> {
                    // 通常の円形（既に描画済み）
                }
            }
        }
    }
}

@Composable
fun CheckInCompletionDialog(
    goal: GoalItem,
    checkIn: CheckInItem,
    changeAmount: Double,
    previousValue: Double,
    progressIncrease: Int,
    onShare: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isGoalCompleted = checkIn.progressPercent >= 100
    var showConfetti by remember { mutableStateOf(isGoalCompleted) }

    // 紙吹雪を数秒後に自動的に停止
    LaunchedEffect(isGoalCompleted) {
        if (isGoalCompleted) {
            showConfetti = true
            kotlinx.coroutines.delay(5000) // 5秒間表示
            showConfetti = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 紙吹雪アニメーション（目標達成時のみ）
        if (showConfetti) {
            ConfettiAnimation(
                modifier = Modifier.fillMaxSize()
            )
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = if (isGoalCompleted) "🎉 目標達成！" else "✅ チェックイン完了！",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isGoalCompleted)
                            "おめでとうございます！目標を達成しました！"
                        else
                            "チェックインが完了しました。",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    // 今回の成果カード
                    if (changeAmount != 0.0 || progressIncrease != 0) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "今回の成果",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )

                                // 数値の変更量を表示
                                val changeText = when {
                                    changeAmount > 0 -> "+${formatNumber(changeAmount, goal.isDecimal)} ${goal.unit}"
                                    changeAmount < 0 -> "${formatNumber(changeAmount, goal.isDecimal)} ${goal.unit}"
                                    else -> "変化なし"
                                }

                                Text(
                                    text = changeText,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        changeAmount > 0 -> Color(0xFF4CAF50) // Green
                                        changeAmount < 0 -> Color(0xFFF44336) // Red
                                        else -> MaterialTheme.colorScheme.onTertiaryContainer
                                    }
                                )

                                // 進捗率の増加を表示
                                if (progressIncrease != 0) {
                                    Text(
                                        text = if (progressIncrease > 0)
                                            "進捗率: +${progressIncrease}%"
                                        else
                                            "進捗率: ${progressIncrease}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = when {
                                            progressIncrease > 0 -> Color(0xFF4CAF50)
                                            progressIncrease < 0 -> Color(0xFFF44336)
                                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 目標達成時のお祝いメッセージを表示
                    if (isGoalCompleted && !goal.celebration.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Enjoy your celebration: 🥳",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = goal.celebration!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // 記入内容の表示
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "記入内容",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("目標:")
                                Text(goal.title, fontWeight = FontWeight.Medium)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("進捗:")
                                Text("${checkIn.progressPercent}%", fontWeight = FontWeight.Medium)
                            }

                            if (checkIn.comment.isNotBlank()) {
                                Column {
                                    Text("コメント:", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = checkIn.comment,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("ホームに戻る")
                    }

                    Button(
                        onClick = {
                            val shareText = buildString {
                                if (isGoalCompleted) {
                                    appendLine("🎉 目標達成しました！")
                                    if (!goal.celebration.isNullOrBlank()) {
                                        appendLine("🥳 ${goal.celebration}")
                                    }
                                } else {
                                    appendLine("📈 進捗更新")
                                }
                                appendLine()
                                appendLine("目標: ${goal.title}")
                                appendLine("進捗: ${checkIn.progressPercent}%")

                                // 今回の成果を追加
                                if (changeAmount != 0.0 || progressIncrease != 0) {
                                    appendLine()
                                    appendLine("✨ 今回の成果:")
                                    if (changeAmount != 0.0) {
                                        val changeText = when {
                                            changeAmount > 0 -> "+${formatNumber(changeAmount, goal.isDecimal)} ${goal.unit}"
                                            changeAmount < 0 -> "${formatNumber(changeAmount, goal.isDecimal)} ${goal.unit}"
                                            else -> "変化なし"
                                        }
                                        appendLine("数値変化: $changeText")
                                    }
                                    if (progressIncrease != 0) {
                                        val progressText = if (progressIncrease > 0)
                                            "+${progressIncrease}%"
                                        else
                                            "${progressIncrease}%"
                                        appendLine("進捗率: $progressText")
                                    }
                                }

                                if (checkIn.comment.isNotBlank()) {
                                    appendLine()
                                    appendLine("💭 ${checkIn.comment}")
                                }
                                appendLine()
                                appendLine("#Litmo #リトモ #目標達成 #進捗 #モチベーション")
                            }
                            onShare(shareText)
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("成果を共有")
                    }
                }
            }
        )
    }
}
