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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

data class FinalCheckInState(
    val goalId: UUID,
    val goalTitle: String,
    val finalProgress: String = "",
    val achievements: String = "",
    val challenges: String = "",
    val learnings: String = ""
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
                    finalProgress = goal.currentProgress.toString()
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
                
                when {
                    currentStep < finalCheckIns.size -> {
                        // Individual goal review steps
                        FinalCheckInStep(
                            checkInState = finalCheckIns[currentStep],
                            onUpdate = { updatedState ->
                                finalCheckIns = finalCheckIns.toMutableList().apply {
                                    set(currentStep, updatedState)
                                }
                            }
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
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Navigation buttons
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
                                enabled = finalCheckIns[currentStep].let { 
                                    it.finalProgress.isNotBlank() && 
                                    it.achievements.isNotBlank()
                                    // Removed strict validation for challenges and learnings
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
                                            learnings = checkInState.learnings
                                        )
                                        viewModel.insertFinalCheckIn(finalCheckIn)
                                        
                                        // Update goal progress
                                        val goal = monthGoals.find { it.id == checkInState.goalId }
                                        goal?.let {
                                            val updatedGoal = it.copy(
                                                currentProgress = checkInState.finalProgress.toIntOrNull() ?: it.currentProgress,
                                                isCompleted = (checkInState.finalProgress.toIntOrNull() ?: 0) >= 100
                                            )
                                            viewModel.updateGoalItem(updatedGoal)
                                        }
                                    }
                                    
                                    // Navigate to summary
                                    navController.navigate("monthlyReviewSummary/$year/$month")
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

@Composable
fun FinalCheckInStep(
    checkInState: FinalCheckInState,
    onUpdate: (FinalCheckInState) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
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
        }
        
        item {
            OutlinedTextField(
                value = checkInState.finalProgress,
                onValueChange = { text ->
                    val progress = text.toIntOrNull()
                    if (progress == null && text.isNotEmpty()) return@OutlinedTextField
                    if (progress != null && (progress < 0 || progress > 100)) return@OutlinedTextField
                    onUpdate(checkInState.copy(finalProgress = text))
                },
                label = { Text("Final Progress (%)") },
                placeholder = { Text("Enter final progress percentage") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            OutlinedTextField(
                value = checkInState.achievements,
                onValueChange = { onUpdate(checkInState.copy(achievements = it)) },
                label = { Text("What did you achieve? *") },
                placeholder = { Text("Describe your accomplishments and successes") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            OutlinedTextField(
                value = checkInState.challenges,
                onValueChange = { onUpdate(checkInState.copy(challenges = it)) },
                label = { Text("What were the challenges? (Optional)") },
                placeholder = { Text("Describe difficulties and obstacles you faced") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            OutlinedTextField(
                value = checkInState.learnings,
                onValueChange = { onUpdate(checkInState.copy(learnings = it)) },
                label = { Text("What did you learn? (Optional)") },
                placeholder = { Text("Reflect on insights and lessons learned") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun OverallReflectionStep(
    reflection: String,
    onReflectionChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
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
        }
        
        item {
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
}
