package com.ablomm.monthlygoalmanager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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
    val goalItem =goalId?.let{viewModel.getGoalById(it)}

    var goalItemState by remember { mutableStateOf(goalItem) }
    var dropMenuExpanded by remember {mutableStateOf(false)}
    var isChecked by remember {mutableStateOf(false)}
    val scrollPosition = rememberScrollState()
    
    // Focus management
    val focusManager = LocalFocusManager.current
    val goalDescriptionFocus = remember { FocusRequester() }
    val targetValueFocus = remember { FocusRequester() }
    val currentProgressFocus = remember { FocusRequester() }
    val associatedGoalFocus = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Goal")
                }
            )

        }
    ) {
        if (goalItem != null) {
            Column(
                modifier = Modifier
                    .padding(it).verticalScroll(scrollPosition)
            ) {
                TextField(
                    modifier = Modifier
                        .padding(15.dp)
                        .focusRequester(goalDescriptionFocus),
                    value = goalItemState!!.title,
                    onValueChange = { goalItemState = goalItemState!!.copy(title = it) },
                    label = { Text("Goal Description") },
                    minLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { targetValueFocus.requestFocus() }
                    )
                )
                //ToDo targetMonth
                TextField(
                    modifier = Modifier
                        .padding(15.dp)
                        .focusRequester(targetValueFocus),
                    value = goalItemState!!.targetValue,
                    onValueChange = { goalItemState = goalItemState!!.copy(targetValue = it) },
                    label = { Text("Target Value") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { currentProgressFocus.requestFocus() }
                    )
                )
                TextField(
                    modifier = Modifier
                        .padding(15.dp)
                        .focusRequester(currentProgressFocus),
                    value = goalItemState!!.currentProgress.toString(),
                    onValueChange = { 
                        try {
                            goalItemState = goalItemState!!.copy(currentProgress = it.toIntOrNull() ?: 0)
                        } catch (e: Exception) {
                            // Handle invalid input gracefully
                            goalItemState = goalItemState!!.copy(currentProgress = 0)
                        }
                    },
                    label = { Text("Current Progress") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { associatedGoalFocus.requestFocus() }
                    )
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
                    modifier = Modifier
                        .padding(15.dp)
                        .focusRequester(associatedGoalFocus),
                    value = " ",
                    onValueChange = { /* To Do */ },
                    label = { Text("Associated Hi-level Goal") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                Button(
                    modifier = Modifier.padding(20.dp).align(Alignment.End),
                    onClick = {
                        viewModel.updateGoalItem(goalItemState!!.copy())
                        navController.popBackStack()
                    }) {
                    Text("Add")
                }

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