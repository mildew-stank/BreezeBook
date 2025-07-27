package com.bsi.breezebook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 2.dp,
    color: Color = MaterialTheme.colorScheme.surface
) {
    Box(
        modifier
            .fillMaxHeight()
            .width(thickness)
            .background(color = color),
    )
}

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 2.dp,
    color: Color = MaterialTheme.colorScheme.outline
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color = color),
    )
}
