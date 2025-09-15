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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
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
import androidx.compose.ui.unit.Dp
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
                    var showDropdownMenu by remember { mutableStateOf(false) }

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

                    IconButton(onClick = { showDropdownMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }

                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Review") },
                            onClick = {
                                showDropdownMenu = false
                                navController.navigate("monthlyReviewWizard/$year/$month")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showDropdownMenu = false
                                monthlyReview?.let { review ->
                                    viewModel.deleteMonthlyReview(review)
                                    navController.popBackStack()
                                }
                            }
                        )
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
                items(goals) { goal ->
                    val finalCheckIn = finalCheckIns.find { it.goalId == goal.id }
                    if (finalCheckIn != null) {
                        ReviewGoalCard(goal = goal, finalCheckIn = finalCheckIn)
                    }
                }
                
                // Monthly Reflectionを一番下に移動
                item {
                    OverallReflectionCard(reflection = monthlyReview?.overallReflection ?: "")
                }
            }
        }
    }
}

@Composable
fun ReviewGoalCard(goal: GoalItem, finalCheckIn: FinalCheckIn) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(6.dp))
                    // 満足度（ヘッダーなしのコンパクト表示）
                    InlineStars(rating = finalCheckIn.satisfactionRating)
                }
                RingProgressMini(
                    fraction = (finalCheckIn.finalProgress / 100f).coerceIn(0f, 1f),
                    label = "${finalCheckIn.finalProgress}%"
                )
            }

            // 折りたたみトグル
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text("Details")
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))

                if (finalCheckIn.achievements.isNotBlank()) {
                    ReviewSection(title = "✅ Achievements", content = finalCheckIn.achievements)
                }
                if (finalCheckIn.challenges.isNotBlank()) {
                    ReviewSection(title = "🔥 Challenges", content = finalCheckIn.challenges)
                }
                if (finalCheckIn.learnings.isNotBlank()) {
                    ReviewSection(title = "💡 Learnings", content = finalCheckIn.learnings)
                }
            }
        }
    }
}

@Composable
private fun ReviewSection(title: String, content: String) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
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
    }
}

@Composable
private fun RingProgressMini(
    fraction: Float,
    size: Dp = 64.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    label: String
) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = this.size.minDimension * 0.12f
            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
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
private fun InlineStars(rating: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (index < rating)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
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
