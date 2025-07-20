package com.bsi.breezeplot.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.bsi.breezeplot.ui.graphics.FilledWave
import com.bsi.breezeplot.ui.graphics.wavyLines
import kotlinx.coroutines.delay

@Composable
fun MainTemplate(
    enableLongPress: Boolean = false,
    showButtons: Boolean = true,
    maskBottomWave: Boolean = false,
    buttonTextLeft: String = "Back",
    buttonTextRight: String = "Next",
    buttonLeftEnabled: Boolean = true,
    buttonRightEnabled: Boolean = true,
    onShowContent: () -> Unit = {},
    onClickLeft: () -> Unit = {},
    onClickRight: () -> Unit = {},
    onLongClick: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(400)
        showContent = true
        onShowContent()
    }
    Surface(
        Modifier
            .fillMaxSize()
            .combinedClickable(
                enabled = enableLongPress,
                onClick = {},
                onLongClick = onLongClick,
                hapticFeedbackEnabled = false
            ), color = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier
                .systemBarsPadding()
                .padding(top = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = wavyLines,
                    contentDescription = "Wavy lines",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                )
                content()
                if (maskBottomWave) {
                    FilledWave(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                }
            }
            Row(
                Modifier
                    .padding(12.dp)
                    .alpha(if (showButtons) 1f else 0f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ButtonCard(
                    text = buttonTextLeft,
                    onClick = onClickLeft,
                    modifier = Modifier.weight(1.0f),
                    containerColor = MaterialTheme.colorScheme.surface,
                    enabled = buttonLeftEnabled || !showButtons
                )
                ButtonCard(
                    text = buttonTextRight,
                    onClick = onClickRight,
                    modifier = Modifier.weight(1.0f),
                    containerColor = MaterialTheme.colorScheme.surface,
                    enabled = buttonRightEnabled || !showButtons
                )
            }

        }
    }
}
