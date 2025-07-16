package com.bsi.breezeplot.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
