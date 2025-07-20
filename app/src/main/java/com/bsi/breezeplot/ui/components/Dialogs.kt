package com.bsi.breezeplot.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
        textContentColor = MaterialTheme.colorScheme.onSurface,
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
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = value,
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
        textContentColor = MaterialTheme.colorScheme.onSurface,
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
