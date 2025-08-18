package com.ablomm.monthlygoalmanager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

// 整数の進捗率を小数点一桁まで繰り上がりで表示するヘルパー関数
private fun formatProgressPercentageFromInt(progressPercent: Int): String {
    val progressDouble = progressPercent.toDouble()
    return String.format("%.1f", progressDouble)
}

data class FinalCheckInState(
    val goalId: UUID,
    val goalTitle: String,
    val finalProgress: String = "",
    val achievements: String = "",
    val challenges: String = "",
    val learnings: String = "",
    val satisfactionRating: Int = 3 // 満足度評価（1-5の星評価）
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthlyReviewWizard(
    year: Int,
    month: Int,
    viewModel: GoalsViewModel,
    navController: NavHostController
) {
    var currentStep by remember { mutableStateOf(0) }
    var monthlyReview by remember { mutableStateOf<MonthlyReview?>(null) }
    var finalCheckIns by remember { mutableStateOf<List<FinalCheckInState>>(emptyList()) }
    var overallReflection by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    val goalListState = viewModel.goalList.collectAsState(initial = emptyList())
    
    // その月の目標をフィルタリング
    val monthGoals = goalListState.value.filter { goal ->
        val goalYear = goal.targetMonth / 1000
        val goalMonth = goal.targetMonth % 1000
        goalYear == year && goalMonth == month
    }
    
    val yearMonth = YearMonth.of(year, month)
    val monthYearText = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    
    LaunchedEffect(monthGoals) {
        if (monthGoals.isNotEmpty()) {
            finalCheckIns = monthGoals.map { goal ->
                FinalCheckInState(
                    goalId = goal.id,
                    goalTitle = goal.title,
                    finalProgress = goal.currentProgress.toString(),
                    achievements = "",
                    challenges = "",
                    learnings = "",
                    satisfactionRating = 3
                )
            }
        }
        
        // 既存の月次レビューがあるかチェック
        monthlyReview = viewModel.getMonthlyReview(year, month)
        monthlyReview?.let { review ->
            overallReflection = review.overallReflection
        }
        
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Review - $monthYearText") },
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
        } else if (monthGoals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📋",
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
                        text = "Add some goals first to conduct a monthly review",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Progress indicator
                LinearProgressIndicator(
                    progress = (currentStep + 1) / (finalCheckIns.size + 1).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                
                Text(
                    text = "Step ${currentStep + 1} of ${finalCheckIns.size + 1}",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        currentStep < finalCheckIns.size -> {
                            // Individual goal review steps
                            FinalCheckInStep(
                                checkInState = finalCheckIns[currentStep],
                                onUpdate = { updatedState ->
                                    finalCheckIns = finalCheckIns.toMutableList().apply {
                                        set(currentStep, updatedState)
                                    }
                                },
                                viewModel = viewModel
                            )
                        }
                        else -> {
                            // Overall reflection step
                            OverallReflectionStep(
                                reflection = overallReflection,
                                onReflectionChange = { overallReflection = it }
                            )
                        }
                    }
                }
                
                // Navigation buttons at the bottom
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { 
                                if (currentStep > 0) {
                                    currentStep--
                                }
                            },
                            enabled = currentStep > 0
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Previous")
                        }
                        
                        when {
                            currentStep < finalCheckIns.size -> {
                                // Next button for goal steps
                                Button(
                                    onClick = { currentStep++ },
                                    enabled = finalCheckIns[currentStep].let { checkIn ->
                                        // シンプル目標の場合は進捗率チェック不要
                                        if (checkIn.achievements.isNotBlank()) {
                                            true
                                        } else {
                                            checkIn.finalProgress.isNotBlank() &&
                                            checkIn.achievements.isNotBlank()
                                        }
                                    }
                                ) {
                                    Text("Next")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                                }
                            }
                            else -> {
                                // Complete button for final step
                                Button(
                                    onClick = {
                                        // Save monthly review
                                        val review = monthlyReview?.copy(
                                            overallReflection = overallReflection
                                        ) ?: MonthlyReview(
                                            year = year,
                                            month = month,
                                            overallReflection = overallReflection
                                        )
                                        
                                        viewModel.insertMonthlyReview(review)
                                        
                                        // Save final check-ins
                                        finalCheckIns.forEach { checkInState ->
                                            val finalCheckIn = FinalCheckIn(
                                                goalId = checkInState.goalId,
                                                monthlyReviewId = review.id,
                                                finalProgress = checkInState.finalProgress.toIntOrNull() ?: 0,
                                                achievements = checkInState.achievements,
                                                challenges = checkInState.challenges,
                                                learnings = checkInState.learnings,
                                                satisfactionRating = checkInState.satisfactionRating
                                            )
                                            viewModel.insertFinalCheckIn(finalCheckIn)
                                            
                                            // Update goal progress - シンプル目標と数値目標で分けて処理
                                            val goal = monthGoals.find { it.id == checkInState.goalId }
                                            goal?.let {
                                                val updatedGoal = it.copy(
                                                    currentProgress = checkInState.finalProgress.toIntOrNull() ?: it.currentProgress,
                                                    isCompleted = (checkInState.finalProgress.toIntOrNull() ?: 0) >= 100
                                                )
                                                viewModel.updateGoalItem(updatedGoal)
                                            }
                                        }

                                        // Show completion dialog instead of direct navigation
                                        showCompletionDialog = true
                                    },
                                    enabled = overallReflection.isNotBlank()
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Complete Review")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Completion dialog with next month guidance
    if (showCompletionDialog) {
        val nextMonth = if (month == 12) 1 else month + 1
        val nextYear = if (month == 12) year + 1 else year
        val nextMonthYearMonth = YearMonth.of(nextYear, nextMonth)
        val nextMonthText = nextMonthYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

        AlertDialog(
            onDismissRequest = { showCompletionDialog = false },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "月次レビュー完了！",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${monthYearText}の月次レビューが完了しました！",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    Divider()

                    Text(
                        text = "🚀 次のステップとして、${nextMonthText}の目標を設定しましょう！",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "継続的な成長のために、次の月の目標設定をお勧めします。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 翌月の目標設定ボタン（メインアクション）
                    Button(
                        onClick = {
                            showCompletionDialog = false
                            // ViewModelの更新は不要。ナビゲーションで直接年月を渡す
                            navController.navigate("home?year=${nextYear}&month=${nextMonth}") {
                                popUpTo("monthlyReview/$year/$month") { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${nextMonthText}の目標へ")
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // サマリーを見るボタン
                        OutlinedButton(
                            onClick = {
                                showCompletionDialog = false
                                navController.navigate("monthlyReviewSummary/$year/$month")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("サマリーを見る")
                        }

                        // 後でやるボタン
                        TextButton(
                            onClick = {
                                showCompletionDialog = false
                                navController.popBackStack()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("後でやる")
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun FinalCheckInStep(
    checkInState: FinalCheckInState,
    onUpdate: (FinalCheckInState) -> Unit,
    viewModel: GoalsViewModel
) {
    var showNoHistoryDialog by remember { mutableStateOf(false) }
    
    // チェックイン履歴を取得
    val checkInsState = viewModel.getCheckInsForGoal(checkInState.goalId).collectAsState(initial = emptyList())
    val hasCheckInHistory = checkInsState.value.isNotEmpty()
    
    // 最後のチェックイン内容を転記する関数
    fun copyLastCheckIn() {
        val checkIns = checkInsState.value
        if (checkIns.isEmpty()) {
            showNoHistoryDialog = true
            return
        }
        
        val lastCheckIn = checkIns.maxByOrNull { it.checkInDate }
        lastCheckIn?.let { 
            // 進捗率を小数点一桁まで繰り上がりで表示
            val formattedProgress = formatProgressPercentageFromInt(it.progressPercent)
            onUpdate(checkInState.copy(
                finalProgress = formattedProgress,
                achievements = it.comment,
                challenges = "",
                learnings = ""
            ))
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Final Check-in",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = checkInState.goalTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // 進捗入力部分 - 目標タイプに応じて表示を変更
        OutlinedTextField(
            value = checkInState.finalProgress,
            onValueChange = { text ->
                val progress = text.toIntOrNull()
                if (progress == null && text.isNotEmpty()) return@OutlinedTextField
                if (progress != null && (progress < 0 || progress > 100)) return@OutlinedTextField
                onUpdate(checkInState.copy(finalProgress = text))
            },
            label = { Text("Final Progress (%) *") },
            placeholder = { Text("Enter final progress percentage") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = checkInState.achievements,
            onValueChange = { onUpdate(checkInState.copy(achievements = it)) },
            label = { Text("What did you achieve? *") },
            placeholder = { Text("Describe your accomplishments and successes") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = checkInState.challenges,
            onValueChange = { onUpdate(checkInState.copy(challenges = it)) },
            label = { Text("What were the challenges? (Optional)") },
            placeholder = { Text("Describe difficulties and obstacles you faced") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = checkInState.learnings,
            onValueChange = { onUpdate(checkInState.copy(learnings = it)) },
            label = { Text("What did you learn? (Optional)") },
            placeholder = { Text("Reflect on insights and lessons learned") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        // 満足度評価（星5つ）
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "自己評価",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "この目標への取り組みにどの程度満足していますか？",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = {
                                onUpdate(checkInState.copy(satisfactionRating = star))
                            }
                        ) {
                            Icon(
                                imageVector = if (star <= checkInState.satisfactionRating)
                                    Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star 星",
                                tint = if (star <= checkInState.satisfactionRating)
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "${checkInState.satisfactionRating}/5 - ${
                        when(checkInState.satisfactionRating) {
                            1 -> "非常に不満"
                            2 -> "不満"
                            3 -> "普通"
                            4 -> "満足"
                            5 -> "非常に満足"
                            else -> "未評価"
                        }
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
    
    // チェックイン履歴がない場合のダイアログ
    if (showNoHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showNoHistoryDialog = false },
            title = { Text("No Check-in History") },
            text = { Text("There are no previous check-ins to copy from. Please create your final check-in manually.") },
            confirmButton = {
                TextButton(
                    onClick = { showNoHistoryDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun OverallReflectionStep(
    reflection: String,
    onReflectionChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📝",
                    fontSize = 32.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Overall Monthly Reflection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reflect on your overall experience this month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        OutlinedTextField(
            value = reflection,
            onValueChange = onReflectionChange,
            label = { Text("Monthly Reflection") },
            placeholder = { Text("How was this month overall? What are your key takeaways? What would you do differently?") },
            minLines = 6,
            maxLines = 10,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
