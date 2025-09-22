package com.ablomm.monthlygoalmanager

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
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

    val focusRequester = remember { FocusRequester() }

    // フォーカス
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // UI
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .imePadding()
    ) {
        // ヘッダー
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Add goal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Set a clear numeric target to track your progress.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Close") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 目標タイトル
        OutlinedTextField(
            value = goalTitle,
            onValueChange = { goalTitle = it },
            label = { Text("Goal name") },
            placeholder = { Text("e.g., Read 10 books") },
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

        // 数値設定
        Text(
            text = "Goal metrics",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        val targetError = targetValueText.isNotBlank() && targetValueText.toDoubleOrNull() == null
        val startError = startValueText.isNotBlank() && startValueText.toDoubleOrNull() == null
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = targetValueText,
                onValueChange = { targetValueText = it },
                label = { Text("Target value") },
                placeholder = { Text(if (isDecimal) "e.g., 100.0" else "e.g., 100") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = targetError,
                supportingText = { if (targetError) Text("Enter a valid number") else Text("Required") },
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
                placeholder = { Text(if (isDecimal) "e.g., 0.0" else "e.g., 0") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = startError,
                supportingText = { if (startError) Text("Enter a valid number") else Text("Defaults to 0") },
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
                label = { Text("Unit") },
                placeholder = { Text("e.g., %, km, pages") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Allow decimals")
                    Text(
                        text = "Use decimal numbers like 2.5",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = isDecimal, onCheckedChange = { isDecimal = it })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 追加ボタン
        val addEnabled = goalTitle.isNotBlank() && targetValueText.toDoubleOrNull() != null
        Button(
            onClick = {
                if (addEnabled) {
                    val targetVal = targetValueText.toDoubleOrNull() ?: 0.0
                    val startVal = startValueText.toDoubleOrNull() ?: 0.0
                    addNewGoal(
                        viewModel = viewModel,
                        title = goalTitle,
                        targetMonth = targetMonth,
                        higherGoalId = null, // removed in MVP
                        displayOrder = displayOrder,
                        targetNumericValue = targetVal,
                        startNumericValue = startVal,
                        unit = unitText,
                        isDecimal = isDecimal,
                        isKeyGoal = false, // removed in MVP
                        detailedDescription = null, // removed in MVP
                        celebration = null // removed in MVP
                    )
                    goalTitle = ""
                    targetValueText = ""
                    startValueText = "0"
                    unitText = ""
                    isDecimal = false
                    onClose()
                }
            },
            enabled = addEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(MaterialTheme.shapes.large)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add goal")
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
        currentNumericValue = startNumericValue,
        unit = unit,
        isKeyGoal = isKeyGoal,
        displayOrder = displayOrder,
        higherGoalId = higherGoalId,
        celebration = celebration,
        isDecimal = isDecimal
    )
    viewModel.addGoalItem(newGoal)
}
