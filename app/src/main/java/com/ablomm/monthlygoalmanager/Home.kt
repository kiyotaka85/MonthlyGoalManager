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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.material3.*
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.UUID
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.YearMonth
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

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
    
    // 現在のルートを取得
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // タブバーを表示するルート
    val tabRoutes = listOf("home", "higherGoals", "settings")
    val shouldShowBottomBar = currentRoute in tabRoutes
    
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Star, contentDescription = "Higher Goals") },
                        label = { Text("Goals") },
                        selected = currentRoute == "higherGoals",
                        onClick = {
                            navController.navigate("higherGoals") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == "settings",
                        onClick = {
                            navController.navigate("settings") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
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

        composable("higherGoals") {
            HigherGoalsScreen(
                navController = navController,
                viewModel = goalsViewModel
            )
        }

        composable("settings") {
            SettingsScreen(
                navController = navController,
                viewModel = goalsViewModel
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
    val higherGoals = viewModel.higherGoalList.collectAsState(initial = emptyList())
    
    val context = LocalContext.current
    
    // 現在表示中の年月を管理 - ViewModelに保存して状態を保持
    val currentYearMonth by viewModel.currentYearMonth.collectAsState(initial = YearMonth.now())
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
                                viewModel.setCurrentYearMonth(currentYearMonth.minusMonths(1))
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowLeft, 
                                contentDescription = "Previous Month"
                            )
                        }
                        
                        // Month and year title
                        Text(
                            text = monthYearText,
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        // Next month button
                        IconButton(
                            onClick = { 
                                viewModel.setCurrentYearMonth(currentYearMonth.plusMonths(1))
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowRight, 
                                contentDescription = "Next Month"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // レビューが完了している場合は何も表示しない
            if (!hasReviewState.value) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Create Review FAB
                    ExtendedFloatingActionButton(
                        onClick = {
                            navController.navigate("monthlyReview/${currentYearMonth.year}/${currentYearMonth.monthValue}")
                        },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Conduct Month End Review")
                    }
                    
                    // Add Goal FAB
                    ExtendedFloatingActionButton(
                        onClick = {
                            val targetMonth = currentYearMonth.year * 1000 + currentYearMonth.monthValue
                            navController.navigate("edit?targetMonth=$targetMonth")
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Goal")
                    }
                }
            }
        }
    ) { innerPadding ->
        if (hasReviewState.value) {
            // レビューが完了している場合：サマリーを表示
            MonthlyReviewSummaryContent(
                year = currentYearMonth.year,
                month = currentYearMonth.monthValue,
                viewModel = viewModel,
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            // レビューが未完了の場合：目標リストを表示
            GoalListContent(
                filteredGoals = filteredGoals,
                isTipsHidden = isTipsHidden.value,
                viewModel = viewModel,
                navController = navController,
                sortMode = sortMode,
                setSortMode = { sortMode = it },
                showSortMenu = showSortMenu,
                setShowSortMenu = { showSortMenu = it },
                isHideCompletedGoals = isHideCompletedGoals.value,
                higherGoals = higherGoals.value,
                monthYearText = monthYearText,
                context = context,
                modifier = Modifier.padding(innerPadding)
            )
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
                        modifier = Modifier
                            .weight(1f)
                            .clickable { 
                                navController.navigate("edit/${goalItem.id}")
                            },
                        title = goalItem.title,
                        description = goalItem.detailedDescription
                    )
                    GoalStatusArea(
                        modifier = Modifier
                            .width(64.dp)
                            .clickable {
                                navController.navigate("checkin/${goalItem.id}")
                            },
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
            
            // ソートと完了済み目標の制御バー
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 完了済み目標の非表示トグル
                        IconButton(
                            onClick = { viewModel.setHideCompletedGoals(!isHideCompletedGoals) }
                        ) {
                            Icon(
                                imageVector = if (isHideCompletedGoals) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = if (isHideCompletedGoals) "Show completed goals" else "Hide completed goals",
                                tint = if (isHideCompletedGoals) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // PDF出力ボタン
                        IconButton(
                            onClick = {
                                val pdfExporter = PdfExporter(context)
                                val intent = pdfExporter.exportGoalsToPdf(
                                    goals = filteredGoals,
                                    higherGoals = higherGoals,
                                    yearMonth = monthYearText
                                )
                                intent?.let {
                                    context.startActivity(Intent.createChooser(it, "Share Goals PDF"))
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Export to PDF"
                            )
                        }
                        
                        // 並べ替えメニュー
                        Box {
                            IconButton(onClick = { setShowSortMenu(true) }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Sort goals"
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { setShowSortMenu(false) }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Default order") },
                                    onClick = {
                                        setSortMode(SortMode.DEFAULT)
                                        setShowSortMenu(false)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Priority (High → Low)") },
                                    onClick = {
                                        setSortMode(SortMode.PRIORITY)
                                        setShowSortMenu(false)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Progress (High → Low)") },
                                    onClick = {
                                        setSortMode(SortMode.PROGRESS)
                                        setShowSortMenu(false)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // 目標カードリスト
            items(
                items = filteredGoals,
                key = { it.id }
            ) { goalItem ->
                val dismissState = rememberSwipeToDismissBoxState(
                    positionalThreshold = { it * 0.25f },
                    confirmValueChange = { dismissValue ->
                        when (dismissValue) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                navController.navigate("checkin/${goalItem.id}")
                                false
                            }
                            SwipeToDismissBoxValue.EndToStart -> {
                                navController.navigate("edit/${goalItem.id}")
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
                                        Text("📊", fontSize = 24.sp)
                                        Text("Check-in", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(end = 16.dp)
                                    ) {
                                        Text("Edit", color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("✏️", fontSize = 24.sp)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                ) {
                    GoalCard(goalItem = goalItem, navController = navController)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthlyReviewSummaryContent(
    year: Int,
    month: Int,
    viewModel: GoalsViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var monthlyReview by remember { mutableStateOf<MonthlyReview?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    val yearMonth = YearMonth.of(year, month)
    val monthYearText = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    
    // Collect goals state
    val allGoals by viewModel.goalList.collectAsState(initial = emptyList())
    
    // Filter goals for the specific month
    val goals = allGoals.filter { goal ->
        val goalYearMonth = goal.targetMonth
        val goalYear = goalYearMonth / 1000
        val goalMonth = goalYearMonth % 1000
        year == goalYear && month == goalMonth
    }
    
    // データロード
    LaunchedEffect(year, month) {
        monthlyReview = viewModel.getMonthlyReview(year, month)
        isLoading = false
    }
    
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {
            item {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📊 Monthly Result",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // 目標達成状況サマリー
            item {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📊 Goals Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val completedGoals = goals.count { it.isCompleted }
                        val totalGoals = goals.size
                        val averageProgress = if (goals.isNotEmpty()) goals.map { it.currentProgress }.average() else 0.0
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$completedGoals",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Completed", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$totalGoals",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Total", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${averageProgress.toInt()}%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text("Avg Progress", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            
            // レビューコメント
            monthlyReview?.let { review ->
                item {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "💭 Monthly Reflection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = review.overallReflection,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // 目標一覧（簡易版）
            item {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🎯 Goals Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        goals.forEach { goal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goal.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (goal.isCompleted) "✅" else "${goal.currentProgress}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            // 編集・削除ボタン
            item {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚙️ Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    navController.navigate("monthlyReview/$year/$month")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Review")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    // TODO: 削除機能を実装
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: GoalsViewModel
) {
    val isTipsHidden by viewModel.isTipsHidden.collectAsState(initial = false)
    val isHideCompletedGoals by viewModel.isHideCompletedGoals.collectAsState(initial = false)
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // タイトル
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 表示設定
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🖼️ Display Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Tips表示設定
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Show Tips",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Display helpful tips on home screen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = !isTipsHidden,
                                onCheckedChange = { viewModel.setTipsHidden(!it) }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 完了済み目標表示設定
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Show Completed Goals",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Display completed goals in goal list",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = !isHideCompletedGoals,
                                onCheckedChange = { viewModel.setHideCompletedGoals(!it) }
                            )
                        }
                    }
                }
            }
            
            // アプリ情報
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "ℹ️ App Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Version",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "1.0.0",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Monthly Goal Manager",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Track and achieve your monthly goals",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // データ管理
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "💾 Data Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = {
                                // TODO: データエクスポート機能を実装
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Data")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = {
                                // TODO: データインポート機能を実装
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Data")
                        }
                    }
                }
            }
        }
    }
}