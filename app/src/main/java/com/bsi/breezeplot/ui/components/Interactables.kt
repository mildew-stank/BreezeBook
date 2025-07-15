package com.bsi.breezeplot.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bsi.breezeplot.viewmodels.AppTheme
import com.bsi.breezeplot.viewmodels.LogEntry
import kotlinx.coroutines.delay

@Composable
fun ButtonCard(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    disabledContainerColor: Color = MaterialTheme.colorScheme.background.copy(alpha = 0.12f),
    disabledContentColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp,
            focusedElevation = 3.dp,
            hoveredElevation = 4.dp,
            disabledElevation = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        ),
        contentPadding = PaddingValues(12.dp),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun SwipeItem(
    entry: LogEntry,
    segmentDistance: Float = 0f,
    swipeAction: (LogEntry) -> Unit,
) {
    val swipeToDismissState =
        rememberSwipeToDismissBoxState(
            positionalThreshold = { totalDistance -> totalDistance * 0.5f },
            confirmValueChange = {
                when (it) {
                    SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> {
                        true
                    }

                    SwipeToDismissBoxValue.Settled -> {
                        false
                    }
                }
            })

    SwipeToDismissBox(
        modifier = Modifier,
        state = swipeToDismissState,
        backgroundContent = { MaterialTheme.colorScheme.primary }) {
        val currentEntry = rememberUpdatedState(entry)

        if (swipeToDismissState.targetValue != SwipeToDismissBoxValue.Settled && swipeToDismissState.progress == 1.0f) {
            LaunchedEffect(currentEntry.value) {
                delay(300) // Wait for the swipe animation to finish
                swipeAction(currentEntry.value)
            }
        }
        LogEntryCard(
            entry = entry,
            segmentDistance = segmentDistance,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp)
        )
    }
}

@Composable
fun SwitchOption(
    modifier: Modifier = Modifier,
    text: String,
    defaultValue: Boolean = false,
    onValueChange: (Boolean) -> Unit = {}
) {
    var checked by remember { mutableStateOf(defaultValue) }

    Box(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall
            )
            Switch(
                modifier = Modifier.height(48.dp), checked = checked, onCheckedChange = {
                    checked = it
                    onValueChange(it)
                }, colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.secondary,
                    checkedBorderColor = MaterialTheme.colorScheme.secondary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.background,
                    uncheckedBorderColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}

@Composable
fun RadioOptions(
    options: List<String> = listOf("Option A", "Option B", "Option C"),
    selectedOption: String= "Option B",
    onSelected: (String) -> Unit = {}
) {
    Column(Modifier.selectableGroup()) {
        options.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onSelected(text) },
                        role = Role.RadioButton
                    ), verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    modifier = Modifier
                        .height(48.dp)
                        .padding(end = 6.dp),
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.secondary
                    ),
                    selected = (text == selectedOption),
                    onClick = null
                )
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ThemeSelectorRadioGroup(
    modifier: Modifier = Modifier, themes: List<String> = listOf("Theme 1", "Theme 2", "Theme 3")

) {
    // This state should be hoisted to a ViewModel in a real application
    var selectedTheme by remember { mutableStateOf(themes[0]) }

    Column(modifier = modifier) {
        themes.forEach { theme ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedTheme = theme
                        // TODO: Add logic here to update the application's theme
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = theme,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall
                )
                RadioButton(
                    selected = (theme == selectedTheme), colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.secondary
                    ), onClick = {
                        selectedTheme = theme
                        // TODO: Add logic here to update the application's theme
                    })

            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class) // For ExposedDropdownMenuBox
@Composable
fun ThemeSelectorDropdown() {
    val themes = AppTheme.entries//listOf("Calm Water", "High Tide", "Dark Night")
    var expanded by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf(themes[0]) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        //.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box {
            TextField(
                value = selectedTheme.displayName, onValueChange = {},
                //label = { Text(text = "Theme", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground) },
                readOnly = true, textStyle = MaterialTheme.typography.titleSmall, trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        Modifier.clickable { expanded = !expanded })
                }, modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) {
                    expanded = true
                })

            DropdownMenu(
                expanded = expanded, onDismissRequest = { expanded = false }) {
                themes.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                selectionOption.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        onClick = {
                            selectedTheme = selectionOption
                            expanded = false
                            // TODO: Add logic here to update the application's theme
                        },
                        //colors = ExposedDropdownMenuDefaults.itemColors(
                    )
                }
            }
        }
    }
}
