package com.bsi.breezeplot.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bsi.breezeplot.system_handlers.LogEntry
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
