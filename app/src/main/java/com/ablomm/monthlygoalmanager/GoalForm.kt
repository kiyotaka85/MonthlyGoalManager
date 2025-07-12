package com.ablomm.monthlygoalmanager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun GoalForm() {
    var goalItem by remember { mutableStateOf(GoalItem(UUID.randomUUID(), title = " " ))}
    var priorityMenuExpanded by remember { mutableStateOf(false) }
    var missionMenuExpanded by remember { mutableStateOf(false) }
    
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
                    Text("New Goal")
                }
            )

        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
        ) {
            TextField(
                modifier = Modifier
                    .padding(15.dp)
                    .focusRequester(goalDescriptionFocus),
                value = goalItem.title,
                onValueChange = { goalItem = goalItem.copy(title = it) },
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
                value = goalItem.targetValue,
                onValueChange = { goalItem = goalItem.copy(targetValue = it) },
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
                value = goalItem.currentProgress.toString(),
                onValueChange = { 
                    try {
                        goalItem = goalItem.copy(currentProgress = it.toIntOrNull() ?: 0)
                    } catch (e: Exception) {
                        // Handle invalid input gracefully
                        goalItem = goalItem.copy(currentProgress = 0)
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
                expanded = priorityMenuExpanded,
                onExpandedChange = { priorityMenuExpanded = !priorityMenuExpanded }
            ) {
                TextField(
                    modifier = Modifier.menuAnchor(
                        type = MenuAnchorType.PrimaryEditable,
                        enabled = true
                    ),
                    value = goalItem.priority.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Priority") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityMenuExpanded)
                    }
                )
                DropdownMenu(
                    expanded = priorityMenuExpanded,
                    onDismissRequest = { priorityMenuExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text(GoalPriority.Low.name) }, onClick = {
                        goalItem = goalItem.copy(priority = GoalPriority.Low)
                        priorityMenuExpanded = false
                    })
                    DropdownMenuItem(text = { Text(GoalPriority.Middle.name) }, onClick = {
                        goalItem = goalItem.copy(priority = GoalPriority.Middle)
                        priorityMenuExpanded = false
                    })
                    DropdownMenuItem(text = { Text(GoalPriority.High.name) }, onClick = {
                        goalItem = goalItem.copy(priority = GoalPriority.High)
                        priorityMenuExpanded = false
                    })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Optional", modifier = Modifier.padding(15.dp))

            TextField(
                modifier = Modifier
                    .padding(15.dp)
                    .focusRequester(associatedGoalFocus)
                    .clickable { print("tapped") },
                value = "Tap to select or add",
                readOnly = true,
                onValueChange = { /* To Do */ },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = missionMenuExpanded)
                },
                label = { Text("Associated Hi-level Goal") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Button(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.End),
                onClick = {/* ToDo */ }) {
                Text("Add")
            }

        }
    }
}