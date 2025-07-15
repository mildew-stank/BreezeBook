package com.bsi.breezeplot.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bsi.breezeplot.viewmodels.AppTheme
import com.bsi.breezeplot.viewmodels.SettingsViewModel

@Composable
fun PinDialog(
    items: List<Pair<String, String>>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onAction: () -> Unit = {},
    confirmButtonText: String = "Close",
    actionButtonText: String = "",
) {
    AlertDialog(
        textContentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = { onDismiss() },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { (name, value) ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = name,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = value,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(confirmButtonText, style = MaterialTheme.typography.bodySmall)
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction() }) {
                Text(actionButtonText, style = MaterialTheme.typography.bodySmall)
            }
        })
}

@Composable
fun ConfirmationDialog(
    dialogText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonText: String = "Yes",
    dismissButtonText: String = "Cancel"
) {
    AlertDialog(
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = { onDismiss() },
        text = { Text(text = dialogText, style = MaterialTheme.typography.bodySmall) },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(confirmButtonText, style = MaterialTheme.typography.bodySmall)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    dismissButtonText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        })
}

@Composable
fun SettingsDialog(
    items: List<Pair<String, Boolean>>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onAction: () -> Unit = {},
    confirmButtonText: String = "Close",
    actionButtonText: String = "Themes",
) {
    AlertDialog(
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = { onDismiss() },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { (name, value) ->
                    var switchChecked by remember { mutableStateOf(value) }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Switch(
                                checked = switchChecked,
                                onCheckedChange = { switchChecked = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedBorderColor = MaterialTheme.colorScheme.primary,

                                    uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.background,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                        //HorizontalDivider(
                        //    thickness = 1.dp,
                        //    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        //)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(confirmButtonText, style = MaterialTheme.typography.bodySmall)
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction() }) {
                Text(actionButtonText, style = MaterialTheme.typography.bodySmall)
            }
        })
}

/*
@Composable
fun ThemeDialog(
    settingsViewModel: SettingsViewModel = SettingsViewModel(),
    onDismiss: () -> Unit = {},
    onAction: () -> Unit = {},
    confirmButtonText: String = "Close",
    actionButtonText: String = "Back",
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val themes = AppTheme.entries

    AlertDialog(
        textContentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = { onDismiss() },
        text = {
            Column {
                themes.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                settingsViewModel.setSelectedTheme(theme)
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = theme.displayName,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodySmall
                        )
                        RadioButton(
                            selected = (theme == uiState.selectedTheme),
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.secondary
                            ),
                            onClick = {
                                settingsViewModel.setSelectedTheme(theme)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(confirmButtonText, style = MaterialTheme.typography.bodySmall)
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction() }) {
                Text(actionButtonText, style = MaterialTheme.typography.bodySmall)
            }
        }
    )
}
 */
