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

        // --- 上位目標の選択 ---
        Card(
            onClick = {
                // 上位目標選択画面へ遷移
                navController.navigate("higherGoals/select")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 選択された上位目標のアイコン表示
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
                    // ラベルと選択された目標名
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

        Spacer(modifier = Modifier.height(16.dp))

        // --- 目標タイトル入力と追加ボタン ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // テキストフィールド
            OutlinedTextField(
                value = goalTitle,
                onValueChange = { goalTitle = it },
                label = { Text("Enter your new goal...") },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (goalTitle.isNotBlank()) {
                            addNewGoal(viewModel, goalTitle, targetMonth, selectedHigherGoalId, displayOrder)
                            goalTitle = "" // 入力フィールドをクリア
                            onClose()
                        }
                        keyboardController?.hide()
                    }
                ),
                shape = MaterialTheme.shapes.extraLarge,
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 送信（追加）ボタン
            IconButton(
                onClick = {
                    if (goalTitle.isNotBlank()) {
                        addNewGoal(viewModel, goalTitle, targetMonth, selectedHigherGoalId, displayOrder)
                        goalTitle = ""
                        onClose()
                    }
                },
                enabled = goalTitle.isNotBlank(),
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (goalTitle.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Add Goal",
                    tint = if (goalTitle.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun addNewGoal(
    viewModel: GoalsViewModel,
    title: String,
    targetMonth: YearMonth,
    higherGoalId: UUID?,
    displayOrder: Int
) {
    val newGoal = GoalItem(
        title = title,
        targetMonth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            targetMonth.year * 1000 + targetMonth.monthValue
        } else {
            // Fallback for older API levels (though this shouldn't happen due to @RequiresApi)
            java.util.Calendar.getInstance().let { cal ->
                cal.get(java.util.Calendar.YEAR) * 1000 + (cal.get(java.util.Calendar.MONTH) + 1)
            }
        },
        displayOrder = displayOrder
    )
    viewModel.addGoalItem(newGoal)}
