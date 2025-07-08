package com.ablomm.monthlygoalmanager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.UUID
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.YearMonth
import java.util.Locale

enum class SortMode {
    DEFAULT,
    PRIORITY,
    PROGRESS
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val goalsViewModel: GoalsViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            Home(navController = navController, viewModel = goalsViewModel)
        }

        composable(
            route = "edit/{goalId}",
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goalIdString = backStackEntry.arguments?.getString("goalId")
            val goalId: UUID? = goalIdString?.let { UUID.fromString(it) }

            GoalEditForm(
                goalId = goalId,
                viewModel = goalsViewModel,
                navController = navController,
                targetMonth = null
            )
        }

        composable("edit") {
            GoalEditForm(
                goalId = null,
                viewModel = goalsViewModel,
                navController = navController,
                targetMonth = null
            )
        }

        composable(
            route = "edit?targetMonth={targetMonth}",
            arguments = listOf(navArgument("targetMonth") { 
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val targetMonth = backStackEntry.arguments?.getInt("targetMonth") ?: 0
            GoalEditForm(
                goalId = null,
                viewModel = goalsViewModel,
                navController = navController,
                targetMonth = if (targetMonth != 0) targetMonth else null
            )
        }

        composable(
            route = "checkin/{goalId}",
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goalIdString = backStackEntry.arguments?.getString("goalId")
            val goalId: UUID? = goalIdString?.let { UUID.fromString(it) }

            goalId?.let {
                CheckInScreen(
                    goalId = it,
                    viewModel = goalsViewModel,
                    navController = navController
                )
            }
        }

        composable(
            route = "monthlyReview/{year}/{month}",
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("month") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt("year") ?: 2025
            val month = backStackEntry.arguments?.getInt("month") ?: 7

            MonthlyReviewWizard(
                year = year,
                month = month,
                viewModel = goalsViewModel,
                navController = navController
            )
        }

        composable(
            route = "monthlyReviewSummary/{year}/{month}",
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("month") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt("year") ?: 2025
            val month = backStackEntry.arguments?.getInt("month") ?: 7

            MonthlyReviewSummary(
                year = year,
                month = month,
                viewModel = goalsViewModel,
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Home(navController: NavHostController, viewModel: GoalsViewModel) {
    val goalListState = viewModel.goalList.collectAsState(initial = emptyList())
    val isTipsHidden = viewModel.isTipsHidden.collectAsState(initial = false)
    val isHideCompletedGoals = viewModel.isHideCompletedGoals.collectAsState(initial = false)
    
    // 現在表示中の年月を管理
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var sortMode by remember { mutableStateOf(SortMode.DEFAULT) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    // 月次レビューの存在をチェック
    val hasReviewState = viewModel.hasMonthlyReview(
        currentYearMonth.year, 
        currentYearMonth.monthValue
    ).collectAsState(initial = false)
    
    // 現在の年月に基づいてフィルタリング
    val filteredGoals = goalListState.value.filter { goal ->
        val goalYearMonth = goal.targetMonth
        val goalYear = goalYearMonth / 1000
        val goalMonth = goalYearMonth % 1000
        currentYearMonth.year == goalYear && currentYearMonth.monthValue == goalMonth
    }.let { goals ->
        // 完了済み目標の非表示機能
        if (isHideCompletedGoals.value) {
            goals.filter { !it.isCompleted }
        } else {
            goals
        }
    }.let { goals ->
        // 並べ替え機能
        when (sortMode) {
            SortMode.DEFAULT -> goals.sortedBy { it.displayOrder }
            SortMode.PRIORITY -> goals.sortedBy { 
                when (it.priority) {
                    GoalPriority.High -> 0
                    GoalPriority.Middle -> 1
                    GoalPriority.Low -> 2
                }
            }
            SortMode.PROGRESS -> goals.sortedByDescending { it.currentProgress }
        }
    }
    
    val monthYearText = currentYearMonth.format(
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous month button
                        IconButton(
                            onClick = { 
                                currentYearMonth = currentYearMonth.minusMonths(1)
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowLeft, 
                                contentDescription = "Previous Month"
                            )
                        }
                        
                        // Month and year title
                        Text(
                            text = "My Goals - $monthYearText",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        // Next month button
                        IconButton(
                            onClick = { 
                                currentYearMonth = currentYearMonth.plusMonths(1)
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowRight, 
                                contentDescription = "Next Month"
                            )
                        }
                    }
                },
                actions = {
                    // 並べ替えメニュー
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Sort goals"
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Default order") },
                                onClick = {
                                    sortMode = SortMode.DEFAULT
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Priority (High → Low)") },
                                onClick = {
                                    sortMode = SortMode.PRIORITY
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Progress (High → Low)") },
                                onClick = {
                                    sortMode = SortMode.PROGRESS
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                    
                    // 完了済み目標の非表示トグル
                    IconButton(
                        onClick = { viewModel.setHideCompletedGoals(!isHideCompletedGoals.value) }
                    ) {
                        Icon(
                            imageVector = if (isHideCompletedGoals.value) Icons.Default.CheckCircle else Icons.Default.Check,
                            contentDescription = if (isHideCompletedGoals.value) "Show completed goals" else "Hide completed goals",
                            tint = if (isHideCompletedGoals.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Monthly Summary button (only if review exists)
                    if (hasReviewState.value) {
                        TextButton(
                            onClick = {
                                navController.navigate("monthlyReviewSummary/${currentYearMonth.year}/${currentYearMonth.monthValue}")
                            }
                        ) {
                            Text("Summary")
                        }
                    }
                    
                    // Monthly Review button - intelligent navigation
                    TextButton(
                        onClick = {
                            // レビューが存在するかチェックしてから遷移先を決定
                            if (hasReviewState.value) {
                                navController.navigate("monthlyReviewSummary/${currentYearMonth.year}/${currentYearMonth.monthValue}")
                            } else {
                                navController.navigate("monthlyReview/${currentYearMonth.year}/${currentYearMonth.monthValue}")
                            }
                        }
                    ) {
                        Text(if (hasReviewState.value) "Review" else "Create Review")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // 現在選択されている年月を引数として渡す
                    val targetMonth = currentYearMonth.year * 1000 + currentYearMonth.monthValue
                    navController.navigate("edit?targetMonth=$targetMonth")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { innerPadding ->

        if (filteredGoals.isEmpty()) {
            // 空の状態の表示
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // Swipe instruction hint
                if (!isTipsHidden.value) {
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
                
                items(
                    items = filteredGoals,
                    key = { it.id }
                ) { goalItem ->

                    val dismissState = rememberSwipeToDismissBoxState(
                        positionalThreshold = { it * 0.25f },
                        confirmValueChange = { dismissValue ->
                            when (dismissValue) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    // 左から右へのスワイプ：チェックイン画面へ
                                    navController.navigate("checkin/${goalItem.id}")
                                    false // スワイプ状態をリセット
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    // 右から左へのスワイプ：編集画面へ
                                    navController.navigate("edit/${goalItem.id}")
                                    false // スワイプ状態をリセット
                                }
                                else -> false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50).copy(alpha = 0.8f) // Green for check-in
                                SwipeToDismissBoxValue.EndToStart -> Color(0xFF2196F3).copy(alpha = 0.8f) // Blue for edit
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .background(color, shape = RoundedCornerShape(6.dp)),
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
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(start = 16.dp)
                                        ) {
                                            Text(
                                                text = "📊",
                                                fontSize = 24.sp
                                            )
                                            Text(
                                                text = "Check-in",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(end = 16.dp)
                                        ) {
                                            Text(
                                                text = "Edit",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "✏️",
                                                fontSize = 24.sp
                                            )
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    ) {
                        GoalCard(goalItem = goalItem, navController = navController)
                    }

                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun GoalCard(
    goalItem: GoalItem,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GoalTextArea(
                        modifier = Modifier.weight(1f),
                        title = goalItem.title,
                        description = goalItem.detailedDescription
                    )
                    GoalStatusArea(
                        modifier = Modifier.width(64.dp),
                        statusEmoji = when (goalItem.currentProgress) {
                            0 -> "🆕"
                            100 -> "✅"
                            else -> "⏳"
                        },
                        progress = goalItem.currentProgress
                    )
                }
            }
        }
    }
}

@Composable
fun GoalTextArea(
    modifier: Modifier = Modifier,
    title: String,
    description: String?
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold
        )
        if (!description.isNullOrBlank()) {
            Text(
                text = description,
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            )
        }
    }
}

@Composable
fun GoalStatusArea(
    modifier: Modifier = Modifier,
    statusEmoji: String = "🆕",
    progress: Int = 0
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = statusEmoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "$progress %",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}