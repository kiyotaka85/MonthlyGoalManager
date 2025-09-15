package com.ablomm.monthlygoalmanager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.YearMonth
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddGoalSheet(
    viewModel: GoalsViewModel,
    targetMonth: YearMonth,
    onClose: () -> Unit,
    navController: NavController,
    displayOrder: Int
) {
    var goalTitle by remember { mutableStateOf("") }
    var targetValueText by remember { mutableStateOf("") }
    var startValueText by remember { mutableStateOf("0") }
    var unitText by remember { mutableStateOf("") }
    var isDecimal by remember { mutableStateOf(false) }

    var showOptions by remember { mutableStateOf(false) }
    var isKeyGoal by remember { mutableStateOf(false) }
    var detailedDescription by remember { mutableStateOf("") }
    var celebration by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // モーダル表示時にテキストフィールドにフォーカスを当てる
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // `higherGoals/select` から戻ってきた時に選択された上位目標IDを取得
    val selectedHigherGoalId = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_higher_goal_id")
        ?.let { UUID.fromString(it) }

    var selectedHigherGoal by remember { mutableStateOf<HigherGoal?>(null) }

    // selectedHigherGoalId が変更されたときに上位目標を再取得
    LaunchedEffect(selectedHigherGoalId) {
        selectedHigherGoal = if (selectedHigherGoalId != null) {
            viewModel.getHigherGoalById(selectedHigherGoalId)
        } else {
            null
        }
    }


    // UIの組み立て
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .imePadding() // キーボード表示時にコンテンツを上に押し上げる
    ) {
        // --- ヘッダー ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Add New Goal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

            // --- 目標タイトル ---
            OutlinedTextField(
                value = goalTitle,
                onValueChange = { goalTitle = it },
                label = { Text("Enter your new goal...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                shape = MaterialTheme.shapes.extraLarge,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- 数値設定（デフォルト表示） ---
            Text(
                text = "Numeric settings",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = targetValueText,
                    onValueChange = { targetValueText = it },
                    label = { Text("Target value") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = startValueText,
                    onValueChange = { startValueText = it },
                    label = { Text("Start value") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = unitText,
                    onValueChange = { unitText = it },
                    label = { Text("Unit (e.g. %, km)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text("Use decimal", modifier = Modifier.weight(1f))
                    Switch(checked = isDecimal, onCheckedChange = { isDecimal = it })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- オプション（折りたたみ） ---
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                ListItem(
                    headlineContent = { Text("Options") },
                    trailingContent = {
                        Icon(
                            imageVector = if (showOptions) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (showOptions) "Collapse" else "Expand"
                        )
                    },
                    modifier = Modifier.clickable { showOptions = !showOptions }
                )

                if (showOptions) {
                    Divider()
                    // 上位目標の選択
                    Card(
                        onClick = {
                            navController.navigate("higherGoals/select")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selectedHigherGoal != null) MaterialTheme.colorScheme.secondaryContainer
                                            else Color.Transparent
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedHigherGoal != null) {
                                        Icon(
                                            imageVector = GoalIcons.getIconByName(selectedHigherGoal!!.icon),
                                            contentDescription = selectedHigherGoal!!.title,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Higher Goal", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        selectedHigherGoal?.title ?: "Not Selected",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (selectedHigherGoal != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Select Higher Goal")
                        }
                    }

                    // キー目標
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Key goal")
                        Switch(checked = isKeyGoal, onCheckedChange = { isKeyGoal = it })
                    }

                    // 詳細説明
                    OutlinedTextField(
                        value = detailedDescription,
                        onValueChange = { detailedDescription = it },
                        label = { Text("Description (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ご褒美
                    OutlinedTextField(
                        value = celebration,
                        onValueChange = { celebration = it },
                        label = { Text("Celebration (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 追加ボタン（送信）
            val addEnabled = goalTitle.isNotBlank() && targetValueText.isNotBlank()
            Button(
                onClick = {
                    if (addEnabled) {
                        val targetVal = targetValueText.toDoubleOrNull() ?: 0.0
                        val startVal = startValueText.toDoubleOrNull() ?: 0.0
                        addNewGoal(
                            viewModel = viewModel,
                            title = goalTitle,
                            targetMonth = targetMonth,
                            higherGoalId = selectedHigherGoalId,
                            displayOrder = displayOrder,
                            targetNumericValue = targetVal,
                            startNumericValue = startVal,
                            unit = unitText,
                            isDecimal = isDecimal,
                            isKeyGoal = isKeyGoal,
                            detailedDescription = detailedDescription.ifBlank { null },
                            celebration = celebration.ifBlank { null }
                        )
                        goalTitle = ""
                        targetValueText = ""
                        startValueText = "0"
                        unitText = ""
                        isDecimal = false
                        isKeyGoal = false
                        detailedDescription = ""
                        celebration = ""
                        onClose()
                    }
                },
                enabled = addEnabled,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Goal")
            }
    }
}

    private fun addNewGoal(
        viewModel: GoalsViewModel,
        title: String,
        targetMonth: YearMonth,
        higherGoalId: UUID?,
        displayOrder: Int,
        targetNumericValue: Double,
        startNumericValue: Double,
        unit: String,
        isDecimal: Boolean,
        isKeyGoal: Boolean = false,
        detailedDescription: String? = null,
        celebration: String? = null
    ) {
        val computedMonth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            targetMonth.year * 1000 + targetMonth.monthValue
        } else {
            java.util.Calendar.getInstance().let { cal ->
                cal.get(java.util.Calendar.YEAR) * 1000 + (cal.get(java.util.Calendar.MONTH) + 1)
            }
        }
        val newGoal = GoalItem(
            title = title,
            detailedDescription = detailedDescription,
            targetMonth = computedMonth,
            targetNumericValue = targetNumericValue,
            startNumericValue = startNumericValue,
            currentNumericValue = startNumericValue, // 初期現在値は開始値に合わせる
            unit = unit,
            isKeyGoal = isKeyGoal,
            displayOrder = displayOrder,
            higherGoalId = higherGoalId,
            celebration = celebration,
            isDecimal = isDecimal
        )
        viewModel.addGoalItem(newGoal)
    }
