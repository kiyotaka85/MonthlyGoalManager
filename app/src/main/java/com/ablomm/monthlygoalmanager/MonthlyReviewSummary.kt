package com.ablomm.monthlygoalmanager

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthlyReviewSummary(
    year: Int,
    month: Int,
    viewModel: GoalsViewModel,
    navController: NavHostController
) {
    var monthlyReview by remember { mutableStateOf<MonthlyReview?>(null) }
    var goals by remember { mutableStateOf<List<GoalItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    val yearMonth = YearMonth.of(year, month)
    val monthYearText = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    
    // Collect goals state
    val allGoalsState = viewModel.goalList.collectAsState(initial = emptyList())
    
    // Final check-ins state
    val finalCheckInsState = monthlyReview?.let { review ->
        viewModel.getFinalCheckInsForReview(review.id).collectAsState(initial = emptyList())
    }
    val finalCheckIns = finalCheckInsState?.value ?: emptyList()
    
    LaunchedEffect(year, month, allGoalsState.value) {
        monthlyReview = viewModel.getMonthlyReview(year, month)
        
        // Get all goals for this month
        goals = allGoalsState.value.filter { goal ->
            val goalYear = goal.targetMonth / 1000
            val goalMonth = goal.targetMonth % 1000
            goalYear == year && goalMonth == month
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Summary - $monthYearText") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareText = generateShareText(monthYearText, goals, finalCheckIns, monthlyReview?.overallReflection ?: "")
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Monthly Review"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
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
        } else if (monthlyReview == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No monthly review found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MonthlyStatsCard(goals = goals, finalCheckIns = finalCheckIns)
                }
                
                items(goals) { goal ->
                    val finalCheckIn = finalCheckIns.find { it.goalId == goal.id }
                    if (finalCheckIn != null) {
                        GoalSummaryCard(goal = goal, finalCheckIn = finalCheckIn)
                    }
                }
                
                item {
                    OverallReflectionCard(reflection = monthlyReview?.overallReflection ?: "")
                }
            }
        }
    }
}

@Composable
fun MonthlyStatsCard(goals: List<GoalItem>, finalCheckIns: List<FinalCheckIn>) {
    val avgProgress = if (finalCheckIns.isNotEmpty()) {
        finalCheckIns.map { it.finalProgress }.average().toInt()
    } else 0
    
    val completedGoals = finalCheckIns.count { it.finalProgress >= 100 }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎯",
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Monthly Performance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "$avgProgress%",
                    label = "Avg Progress"
                )
                StatItem(
                    value = "$completedGoals/${goals.size}",
                    label = "Completed"
                )
                StatItem(
                    value = "${goals.size}",
                    label = "Total Goals"
                )
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun GoalSummaryCard(goal: GoalItem, finalCheckIn: FinalCheckIn) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Box(
                    modifier = Modifier
                        .background(                        color = when {
                            finalCheckIn.finalProgress >= 100 -> Color.Green.copy(alpha = 0.2f)
                            finalCheckIn.finalProgress >= 75 -> Color.Blue.copy(alpha = 0.2f)
                            finalCheckIn.finalProgress >= 50 -> Color(0xFFFF9800).copy(alpha = 0.2f) // Orange color
                            else -> Color.Red.copy(alpha = 0.2f)
                        },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${finalCheckIn.finalProgress}%",
                        fontWeight = FontWeight.Bold,
                        color = when {
                            finalCheckIn.finalProgress >= 100 -> Color.Green.copy(alpha = 0.8f)
                            finalCheckIn.finalProgress >= 75 -> Color.Blue.copy(alpha = 0.8f)
                            finalCheckIn.finalProgress >= 50 -> Color(0xFFFF9800).copy(alpha = 0.8f) // Orange color
                            else -> Color.Red.copy(alpha = 0.8f)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 満足度評価（星評価）を表示
            SatisfactionRatingDisplay(rating = finalCheckIn.satisfactionRating)

            if (finalCheckIn.achievements.isNotBlank()) {
                SummarySection(
                    title = "✅ Achievements",
                    content = finalCheckIn.achievements
                )
            }
            
            if (finalCheckIn.challenges.isNotBlank()) {
                SummarySection(
                    title = "🔥 Challenges",
                    content = finalCheckIn.challenges
                )
            }
            
            if (finalCheckIn.learnings.isNotBlank()) {
                SummarySection(
                    title = "💡 Learnings",
                    content = finalCheckIn.learnings
                )
            }
        }
    }
}

@Composable
fun SummarySection(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun OverallReflectionCard(reflection: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📝 Monthly Reflection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reflection,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SatisfactionRatingDisplay(rating: Int) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "⭐ 満足度評価",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 星評価を表示
            repeat(5) { index ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (index < rating)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 評価テキスト
            Text(
                text = "${rating}/5 - ${
                    when(rating) {
                        1 -> "非常に不満"
                        2 -> "不満"
                        3 -> "普通"
                        4 -> "満足"
                        5 -> "非常に満足"
                        else -> "未評価"
                    }
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun generateShareText(
    monthYear: String,
    goals: List<GoalItem>,
    finalCheckIns: List<FinalCheckIn>,
    reflection: String
): String {
    val avgProgress = if (finalCheckIns.isNotEmpty()) {
        finalCheckIns.map { it.finalProgress }.average().toInt()
    } else 0
    
    val completedGoals = finalCheckIns.count { it.finalProgress >= 100 }
    
    return buildString {
        appendLine("🎯 My $monthYear Goal Review")
        appendLine()
        appendLine("📊 Performance:")
        appendLine("• Average Progress: $avgProgress%")
        appendLine("• Completed Goals: $completedGoals/${goals.size}")
        appendLine()
        
        goals.forEachIndexed { index, goal ->
            val finalCheckIn = finalCheckIns.find { it.goalId == goal.id }
            if (finalCheckIn != null) {
                appendLine("${index + 1}. ${goal.title}")
                appendLine("   Progress: ${finalCheckIn.finalProgress}%")
                if (finalCheckIn.achievements.isNotBlank()) {
                    appendLine("   ✅ ${finalCheckIn.achievements.take(100)}...")
                }
                appendLine()
            }
        }
        
        if (reflection.isNotBlank()) {
            appendLine("📝 Monthly Reflection:")
            appendLine(reflection.take(200) + if (reflection.length > 200) "..." else "")
            appendLine()
        }
        
        appendLine("#MonthlyGoals #PersonalDevelopment #Progress")
    }
}
