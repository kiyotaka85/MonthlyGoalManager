package com.ablomm.monthlygoalmanager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditForm(
    goalId: UUID?,
    viewModel: GoalsViewModel,
    navController: NavHostController)
{
    var goalItemState by remember { mutableStateOf<GoalItem?>(null) }

    // 2. LaunchedEffectを使って、初回描画時またはgoalIdが変更された時に一度だけ実行
    LaunchedEffect(key1 = goalId) {
        if (goalId == null) {
            // ▼ 新規作成モード ▼
            // 新しい空のGoalItemを生成してStateにセット
            goalItemState = GoalItem(id = UUID.randomUUID(), title = "", /* 他の初期値 */)
        } else {
            // ▼ 編集モード ▼
            // 既存のデータをDBから取得してStateにセット
            goalItemState = viewModel.getGoalById(goalId)
        }
    }

    var dropMenuExpanded by remember {mutableStateOf(false)}
    var isChecked by remember {mutableStateOf(false)}
    val scrollPosition = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Goal")
                }
            )

        }
    ) {
        if (goalItemState != null) {
            Column(
                modifier = Modifier
                    .padding(it).verticalScroll(scrollPosition)
            ) {
                TextField(
                    modifier = Modifier.padding(15.dp),
                    value = goalItemState!!.title,
                    onValueChange = { goalItemState = goalItemState!!.copy(title = it) },
                    label = { Text("Goal Description") },
                    minLines = 5
                )
                //ToDo targetMonth
                TextField(
                    modifier = Modifier.padding(15.dp),
                    value = goalItemState!!.targetValue,
                    onValueChange = { goalItemState = goalItemState!!.copy(targetValue = it) },
                    label = { Text("Target Value") })
                TextField(
                    modifier = Modifier.padding(15.dp),
                    value = goalItemState!!.currentProgress.toString(),
                    onValueChange = { text ->
                        // toIntOrNull()で安全に変換。失敗したら0にする
                        val progress = text.toIntOrNull() ?: 0
                        goalItemState = goalItemState!!.copy(currentProgress = progress)
                    },
                    label = { Text("Current Progress") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // 数字キーボードを表示
                )

                ExposedDropdownMenuBox(
                    modifier = Modifier.padding(15.dp),
                    expanded = dropMenuExpanded,
                    onExpandedChange = { dropMenuExpanded = !dropMenuExpanded }
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(
                            type = MenuAnchorType.PrimaryEditable,
                            enabled = true
                        ),
                        value = goalItemState!!.priority.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropMenuExpanded)
                        }
                    )
                    DropdownMenu(
                        expanded = dropMenuExpanded,
                        onDismissRequest = { dropMenuExpanded = false }
                    ) {
                        DropdownMenuItem(text = { Text(GoalPriority.Low.name) }, onClick = {
                            goalItemState = goalItemState!!.copy(priority = GoalPriority.Low)
                            dropMenuExpanded = false
                        })
                        DropdownMenuItem(text = { Text(GoalPriority.Middle.name) }, onClick = {
                            goalItemState = goalItemState!!.copy(priority = GoalPriority.Middle)
                            dropMenuExpanded = false
                        })
                        DropdownMenuItem(text = { Text(GoalPriority.High.name) }, onClick = {
                            goalItemState = goalItemState!!.copy(priority = GoalPriority.High)
                            dropMenuExpanded = false
                        })
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = {
                            isChecked = !isChecked
                        })

                    Text("Completed")
                }


                Spacer(modifier = Modifier.height(24.dp))

                Text("Optional", modifier = Modifier.padding(15.dp))

                TextField(
                    modifier = Modifier.padding(15.dp),
                    value = " ",
                    onValueChange = { /* To Do */ },
                    label = { Text("Associated Hi-level Goal") }
                )

                Button(
                    modifier = Modifier.padding(20.dp).align(Alignment.End),
                    onClick = {
                        goalItemState?.let { currentGoal ->
                            if (goalId == null) {
                                // ▼ 新規作成モードの保存処理 ▼
                                viewModel.addGoalItem(currentGoal)
                            } else {
                                // ▼ 編集モードの保存処理 ▼
                                viewModel.updateGoalItem(currentGoal)
                            }
                            // 保存後は前の画面に戻る
                            navController.popBackStack()
                        }
                    }) {
                    // モードによってボタンのテキストを変える
                    Text(if (goalId == null) "Add" else "Save")
                }

            }


        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // CircularProgressIndicator() などを置くとより親切
            }
        }
    }
}

//@Preview(showBackground = true)
@Composable
fun Test() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = true,
            onCheckedChange = {

            })

        Text("Completed")
    }
}