package com.ablomm.monthlygoalmanager

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    var progressIncreaseDecimal by remember { mutableStateOf(0.0) } // 精密な進捗率増加量を追加

    val context = androidx.compose.ui.platform.LocalContext.current
    val checkInsState = viewModel.getCheckInsForGoal(goalId).collectAsState(initial = emptyList())

    LaunchedEffect(goalId) {
        goalItemState = viewModel.getGoalById(goalId)
        progressPercent = goalItemState?.currentProgress?.toString() ?: "0"
        // 現在値は空の状態で開始（規定で数値を入力しない）
        numericValue = ""
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
                            GoalProgressInfo(
                                goal = goal
                            )
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
                                placeholder = {
                                    if (goal.isDecimal) {
                                        Text("例：${String.format("%.1f", goal.targetNumericValue)}")
                                    } else {
                                        Text("例：${goal.targetNumericValue.toInt()}")
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (goal.isDecimal) KeyboardType.Decimal else KeyboardType.Number
                                ),
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
                                    // 進捗率の増加を小数点で計算
                                    val oldProgressPrecise = calculateProgressPrecise(
                                        goal.startNumericValue,
                                        goal.targetNumericValue,
                                        goal.currentNumericValue
                                    )
                                    val newProgressPrecise = calculateProgressPrecise(
                                        goal.startNumericValue,
                                        goal.targetNumericValue,
                                        currentValue
                                    )
                                    val calculatedProgressIncrease = newProgressPrecise - oldProgressPrecise

                                    // 精密な進捗率増加量も保存
                                    progressIncreaseDecimal = calculatedProgressIncrease

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
            progressIncreaseDecimal = progressIncreaseDecimal, // 精密な進捗率増加量を渡す
            onShare = { shareText ->
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share progress"))
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

    // 進捗率を小数点一桁まで繰り上がりで表示
    val progressText = formatProgressPercentageFromInt(checkIn.progressPercent)

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
                    text = "${progressText}%",
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

// 整数の進捗率を小数点一桁まで繰り上がりで表示するヘルパー関数
// 注意：この関数は整数値しか受け取らないため、精密な値は失われます
// 可能な場合は、元の目標データから精密計算を行うことを推奨
private fun formatProgressPercentageFromInt(progressPercent: Int): String {
    val progressDouble = progressPercent.toDouble()
    return String.format("%.1f", progressDouble)
}

// 進捗率の増加量を小数点一桁まで繰り上がりで表示するヘルパー関数
private fun formatProgressIncrease(progressIncrease: Double): String {
    return if (progressIncrease > 0) {
        val rounded = kotlin.math.ceil(progressIncrease * 10) / 10
        "+${String.format("%.1f", rounded)}%"
    } else if (progressIncrease < 0) {
        val rounded = kotlin.math.floor(progressIncrease * 10) / 10
        "${String.format("%.1f", rounded)}%"
    } else {
        "No change"
    }
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
    progressIncreaseDecimal: Double, // パラメータ名をprogressIncreaseから変更
    onShare: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isGoalCompleted = checkIn.progressPercent >= 100
    var showConfetti by remember { mutableStateOf(isGoalCompleted) }

    // 精密な進捗率を計算（目標データから）
    val preciseProgress = calculateProgressPrecise(
        goal.startNumericValue,
        goal.targetNumericValue,
        goal.currentNumericValue
    )

    // 精密な進捗率を小数点一桁まで繰り上がりで表示
    val formattedProgress = kotlin.math.ceil(preciseProgress * 10) / 10

    // 紙吹雪を数秒後に自動的に停止（控えめに 3 秒）
    LaunchedEffect(isGoalCompleted) {
        if (isGoalCompleted) {
            showConfetti = true
            kotlinx.coroutines.delay(3000)
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
                    text = if (isGoalCompleted) "Goal completed" else "Check-in complete",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Details card (Goal / Progress / Change)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Goal")
                                Text(goal.title, fontWeight = FontWeight.Medium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Progress")
                                Text("${String.format("%.1f", formattedProgress)}%", fontWeight = FontWeight.Medium)
                            }

                            // Change line (value and/or percent)
                            val hasChange = (changeAmount != 0.0) || (progressIncreaseDecimal != 0.0)
                            if (hasChange) {
                                val changeValue = when {
                                    changeAmount > 0 -> "+${formatNumber(changeAmount, goal.isDecimal)} ${goal.unit}"
                                    changeAmount < 0 -> "${formatNumber(changeAmount, goal.isDecimal)} ${goal.unit}"
                                    else -> null
                                }
                                val changePercent = if (progressIncreaseDecimal != 0.0) {
                                    // 例: +1.5%
                                    val sign = if (progressIncreaseDecimal > 0) "+" else ""
                                    "$sign${String.format("%.1f", progressIncreaseDecimal)}%"
                                } else null
                                val changeParts = listOfNotNull(changeValue, changePercent)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Change")
                                    Text(changeParts.joinToString(", "), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // Reward (only when completed)
                    if (isGoalCompleted && !goal.celebration.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Reward",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = goal.celebration!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Note (if exists)
                    if (checkIn.comment.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Note",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = checkIn.comment,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) {
                        Text("Done")
                    }
                    Button(
                        onClick = {
                            val shareText = buildString {
                                if (isGoalCompleted) {
                                    appendLine("🎉 Goal completed!")
                                    if (!goal.celebration.isNullOrBlank()) {
                                        appendLine("Reward: ${goal.celebration}")
                                    }
                                } else {
                                    appendLine("✅ Check-in complete")
                                }
                                appendLine()
                                appendLine("Goal: ${goal.title}")
                                appendLine("Progress: ${String.format("%.1f", formattedProgress)}%")

                                val hasChange = (changeAmount != 0.0) || (progressIncreaseDecimal != 0.0)
                                if (hasChange) {
                                    appendLine()
                                    append("Change: ")
                                    val parts = mutableListOf<String>()
                                    if (changeAmount != 0.0) {
                                        val changeStr = if (changeAmount > 0) "+${formatNumber(changeAmount, goal.isDecimal)} ${goal.unit}" else "${formatNumber(changeAmount, goal.isDecimal)} ${goal.unit}"
                                        parts.add(changeStr)
                                    }
                                    if (progressIncreaseDecimal != 0.0) {
                                        val sign = if (progressIncreaseDecimal > 0) "+" else ""
                                        parts.add("$sign${String.format("%.1f", progressIncreaseDecimal)}%")
                                    }
                                    appendLine(parts.joinToString(", "))
                                }

                                if (checkIn.comment.isNotBlank()) {
                                    appendLine()
                                    appendLine("Note: ${checkIn.comment}")
                                }
                                appendLine()
                                appendLine("#Litmo #MonthlyGoalManager #progress #motivation")
                            }
                            onShare(shareText)
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                }
            }
        )
    }
}

@Composable
fun CustomKeypad(
    modifier: Modifier = Modifier,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    isDecimal: Boolean
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(if (isDecimal) "." else "", "0", "⌫")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                row.forEach { key ->
                    val buttonModifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .weight(1f)
                    if (key.isEmpty()) {
                        Spacer(modifier = buttonModifier)
                    } else {
                        Button(
                            onClick = {
                                if (key == "⌫") onBackspace() else onKeyPress(key)
                            },
                            modifier = buttonModifier,
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            if (key == "⌫") {
                                Icon(Icons.Default.Backspace, contentDescription = "Backspace")
                            } else {
                                Text(key, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- ここから：モーダルシート用の簡易チェックインフォーム ---
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CheckInSheet(
    goalId: UUID,
    viewModel: GoalsViewModel,
    onClose: () -> Unit
) {
    var goal by remember { mutableStateOf<GoalItem?>(null) }
    var currentValueText by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    var showCompletionDialog by remember { mutableStateOf(false) }
    var savedCheckIn by remember { mutableStateOf<CheckInItem?>(null) }
    var changeAmount by remember { mutableStateOf(0.0) }
    var progressIncreaseDecimal by remember { mutableStateOf(0.0) }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(goalId) {
        goal = viewModel.getGoalById(goalId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "New Check-in",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        goal?.let { g ->
            // Display for the current value
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Previous: ${formatNumber(g.currentNumericValue, g.isDecimal)} ${g.unit}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val diff = (currentValueText.toDoubleOrNull() ?: 0.0) - g.currentNumericValue
                val diffText = if (currentValueText.isNotBlank() && diff != 0.0) {
                    val sign = if (diff > 0) "+" else ""
                    "${sign}${formatNumber(diff, g.isDecimal)} ${g.unit}"
                } else {
                    ""
                }
                Text(
                    text = diffText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (diff > 0) Color(0xFF4CAF50) else if (diff < 0) Color(0xFFF44336) else Color.Transparent
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (currentValueText.isEmpty()) "0" else currentValueText,
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = g.unit,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }


            // Custom keypad
            CustomKeypad(
                isDecimal = g.isDecimal,
                onKeyPress = { key ->
                    if (key == ".") {
                        if (!currentValueText.contains(".")) {
                            currentValueText = if (currentValueText.isEmpty()) "0." else currentValueText + "."
                        }
                    } else {
                        if (currentValueText == "0") {
                            currentValueText = key
                        } else {
                            currentValueText += key
                        }
                    }
                },
                onBackspace = {
                    if (currentValueText.isNotEmpty()) {
                        currentValueText = currentValueText.dropLast(1)
                    }
                }
            )

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Comments (Optional)") },
                placeholder = { Text("What did you accomplish? How do you feel?") },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    // This might need a focus manager if we want to dismiss the keyboard
                }),
                modifier = Modifier.fillMaxWidth()
            )

            val canSave = currentValueText.isNotBlank()

            Button(
                onClick = {
                    val currentValue = currentValueText.toDoubleOrNull() ?: return@Button

                    val oldProgressPrecise = calculateProgressPrecise(
                        g.startNumericValue,
                        g.targetNumericValue,
                        g.currentNumericValue
                    )
                    val newProgress = calculateProgress(
                        g.startNumericValue,
                        g.targetNumericValue,
                        currentValue
                    )
                    val newProgressPrecise = calculateProgressPrecise(
                        g.startNumericValue,
                        g.targetNumericValue,
                        currentValue
                    )

                    changeAmount = currentValue - g.currentNumericValue
                    progressIncreaseDecimal = newProgressPrecise - oldProgressPrecise

                    val checkIn = CheckInItem(
                        goalId = g.id,
                        progressPercent = newProgress,
                        comment = comment.trim()
                    )
                    viewModel.addCheckIn(checkIn)

                    val updatedGoal = g.copy(
                        currentNumericValue = currentValue,
                        currentProgress = newProgress,
                        isCompleted = newProgress >= 100
                    )
                    viewModel.updateGoalItem(updatedGoal)

                    savedCheckIn = checkIn
                    showCompletionDialog = true
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Check-in")
            }
        }
    }

    if (showCompletionDialog && savedCheckIn != null && goal != null) {
        CheckInCompletionDialog(
            goal = goal!!,
            checkIn = savedCheckIn!!,
            changeAmount = changeAmount,
            previousValue = goal!!.currentNumericValue,
            progressIncreaseDecimal = progressIncreaseDecimal,
            onShare = { shareText ->
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share progress"))
                onClose()
            },
            onDismiss = {
                onClose()
            }
        )
    }
}
