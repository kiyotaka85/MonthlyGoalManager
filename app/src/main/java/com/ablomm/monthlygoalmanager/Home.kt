package com.ablomm.monthlygoalmanager

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Home(
    navController: NavHostController,
    viewModel: GoalsViewModel,
    targetYear: Int? = null,
    targetMonth: Int? = null
) {
    val goalListState = viewModel.goalList.collectAsState(initial = emptyList())
    val isTipsHidden = viewModel.isTipsHidden.collectAsState(initial = false)
    val isHideCompletedGoals = viewModel.isHideCompletedGoals.collectAsState(initial = false)
    val higherGoals = viewModel.higherGoalList.collectAsState(initial = emptyList())
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ナビゲーション経由で年月が渡された場合、ViewModelの状態を更新する
    LaunchedEffect(targetYear, targetMonth) {
        if (targetYear != null && targetMonth != null) {
            viewModel.setCurrentYearMonth(YearMonth.of(targetYear, targetMonth))
        }
    }

    // 現在表示中の年月を管理 - ViewModelに保存して状態を保持
    val currentYearMonth by viewModel.currentYearMonth.collectAsState(initial = YearMonth.now())
    val isEditableMonth = currentYearMonth == YearMonth.now()

    // 現在の年月に基づいてフィルタリング（デフォルト順のみ）
    val filteredGoals = goalListState.value.filter { goal ->
        val goalYearMonth = goal.targetMonth
        val goalYear = goalYearMonth / 1000
        val goalMonth = goalYearMonth % 1000
        currentYearMonth.year == goalYear && currentYearMonth.monthValue == goalMonth
    }.let { goals ->
        if (isHideCompletedGoals.value) goals.filter { !it.isCompleted } else goals
    }.sortedBy { it.displayOrder }

    val monthYearText = currentYearMonth.format(
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
    )

    // チェックイン用モーダルシートの状態
    var showCheckInSheet by remember { mutableStateOf<Boolean>(false) }
    var targetGoalForCheckIn by remember { mutableStateOf<UUID?>(null) }
    val checkInSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 目標追加用モーダルシートの状態
    var showAddGoalSheet by remember { mutableStateOf<Boolean>(false) }
    val addGoalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 目標編集用モーダルシートの状態
    var showEditGoalSheet by remember { mutableStateOf<Boolean>(false) }
    var targetGoalForEdit by remember { mutableStateOf<UUID?>(null) }
    val editGoalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 年月移動ボタンを左寄せ
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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

                            Text(
                                text = monthYearText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (!isEditableMonth) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked month",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

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
                },
                actions = {
                    // Share summary image directly (no overflow menus)
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val rows = filteredGoals.map { g ->
                                    SummaryGoalRow(
                                        name = g.title,
                                        current = g.currentNumericValue,
                                        target = g.targetNumericValue,
                                        unit = g.unit,
                                        isDecimal = g.isDecimal
                                    )
                                }
                                val bmp = createSummaryCardBitmap(
                                    context = context,
                                    title = buildSummaryTitle(currentYearMonth),
                                    rows = rows
                                )
                                shareSummaryBitmap(context, bmp)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share summary"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // 編集ロック中は表示しない
            if (isEditableMonth) {
                FloatingActionButton(
                    onClick = {
                        showAddGoalSheet = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Goal"
                    )
                }
            }
        }
    ) { innerPadding ->
        // 目標リストを表示（常に）
        GoalListContent(
            filteredGoals = filteredGoals,
            isTipsHidden = isTipsHidden.value,
            viewModel = viewModel,
            navController = navController,
            higherGoals = higherGoals.value,
            onCheckIn = { goalId ->
                if (isEditableMonth) {
                    targetGoalForCheckIn = goalId
                    showCheckInSheet = true
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("This month is locked for editing.")
                    }
                }
            },
            modifier = Modifier.padding(innerPadding),
            onEdit = { goalId ->
                if (isEditableMonth) {
                    targetGoalForEdit = goalId
                    showEditGoalSheet = true
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("This month is locked for editing.")
                    }
                }
            }
        )
    }

    val goalList = viewModel.goalList.collectAsState(initial = emptyList()).value

    if (showAddGoalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddGoalSheet = false },
            sheetState = addGoalSheetState
        ) {
            AddGoalSheet(
                viewModel = viewModel,
                targetMonth = currentYearMonth,
                onClose = { showAddGoalSheet = false },
                navController = navController,
                displayOrder = goalList.size
            )
        }
    }

    if (showEditGoalSheet && targetGoalForEdit != null) {
        val existing = goalList.firstOrNull { it.id == targetGoalForEdit }
        if (existing != null) {
            val ym = YearMonth.of(existing.targetMonth / 1000, existing.targetMonth % 1000)
            ModalBottomSheet(
                onDismissRequest = {
                    showEditGoalSheet = false
                    targetGoalForEdit = null
                },
                sheetState = editGoalSheetState
            ) {
                AddGoalSheet(
                    viewModel = viewModel,
                    targetMonth = ym,
                    onClose = {
                        showEditGoalSheet = false
                        targetGoalForEdit = null
                    },
                    navController = navController,
                    displayOrder = existing.displayOrder,
                    existingGoal = existing
                )
            }
        }
    }

    if (showCheckInSheet && targetGoalForCheckIn != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showCheckInSheet = false
                targetGoalForCheckIn = null
            },
            sheetState = checkInSheetState
        ) {
            CheckInSheet(
                goalId = targetGoalForCheckIn!!,
                viewModel = viewModel,
                onClose = {
                    showCheckInSheet = false
                    targetGoalForCheckIn = null
                }
            )
        }
    }
}