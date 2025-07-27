package com.bsi.breezebook.ui.graphics

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private fun PathBuilder.drawWavyLine(startY: Float) {
    moveToRelative(0.0f, startY)
    curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
    curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
    curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
    curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
    curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
    curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
    reflectiveCurveToRelative(25.4f, -16.93f, 33.87f, -16.93f)
    curveToRelative(8.47f, 0.0f, 18.63f, 16.6f, 33.87f, 16.93f)
}

@Composable
fun GenerateWavyLines(
    modifier: Modifier = Modifier, numberOfLines: Int = 3
) {
    val viewportHeight = 25.4f + (33.87f * (numberOfLines - 1)) + 8.46f
    val defaultHeight = 128.dp * numberOfLines
    val validNumberOfLines = numberOfLines.coerceAtLeast(1)
    val yCoordinates = mutableListOf<Float>()
    var currentY = (validNumberOfLines - 1) * 33.87f + 25.4f

    if (validNumberOfLines > 0) {
        for (i in 0 until validNumberOfLines) {
            yCoordinates.add(currentY)
            currentY -= 33.87f
        }
    }
    val image = ImageVector.Builder(
        name = "WavyLines",
        defaultWidth = 1024.dp,
        defaultHeight = defaultHeight,
        viewportWidth = 270.93f,
        viewportHeight = viewportHeight
    ).apply {
        yCoordinates.forEach { y ->
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1f,
                strokeLineCap = Round,
                strokeLineJoin = StrokeJoin.Companion.Round,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                drawWavyLine(y)
            }
        }
    }.build()

    Icon(
        imageVector = image,
        contentDescription = "Wavy lines",
        tint = MaterialTheme.colorScheme.outline,
        modifier = modifier
    )
}

val wavyLines = ImageVector.Builder(
    name = "WavyLines",
    defaultWidth = 1024.dp,
    defaultHeight = 384.dp,
    viewportWidth = 270.93f,
    viewportHeight = 101.6f
).apply {
    path(
        fill = SolidColor(Color(0x00000000)),
        stroke = SolidColor(Color(0xFF000000)),
        strokeLineWidth = 1f,
        strokeLineCap = Round,
        strokeLineJoin = StrokeJoin.Companion.Round,
        strokeLineMiter = 4.0f,
        pathFillType = NonZero
    ) {
        moveToRelative(0.0f, 93.14f)
        curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
        curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
        curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
        curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
        curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
        curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
        reflectiveCurveToRelative(25.4f, -16.93f, 33.87f, -16.93f)
        curveToRelative(8.47f, 0.0f, 18.63f, 16.6f, 33.87f, 16.93f)
    }
    path(
        fill = SolidColor(Color(0x00000000)),
        stroke = SolidColor(Color(0xFF000000)),
        strokeLineWidth = 1f,
        strokeLineCap = Round,
        strokeLineJoin = StrokeJoin.Companion.Round,
        strokeLineMiter = 4.0f,
        pathFillType = NonZero
    ) {
        moveToRelative(0.0f, 59.27f)
        curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
        curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
        curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
        curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
        curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
        curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
        reflectiveCurveToRelative(25.4f, -16.93f, 33.87f, -16.93f)
        curveToRelative(8.47f, 0.0f, 18.63f, 16.6f, 33.87f, 16.93f)
    }
    path(
        fill = SolidColor(Color(0x00000000)),
        stroke = SolidColor(Color(0xFF000000)),
        strokeLineWidth = 1f,
        strokeLineCap = Round,
        strokeLineJoin = StrokeJoin.Companion.Round,
        strokeLineMiter = 4.0f,
        pathFillType = NonZero
    ) {
        moveToRelative(0.0f, 25.4f)
        curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
        curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
        curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
        curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
        curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
        curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
        reflectiveCurveToRelative(25.4f, -16.93f, 33.87f, -16.93f)
        curveToRelative(8.47f, 0.0f, 18.63f, 16.6f, 33.87f, 16.93f)
    }
}.build()

@Composable
fun FilledWave(modifier: Modifier = Modifier) {
    val wavyFill = ImageVector.Builder(
        name = "WavyFill",
        defaultWidth = 1024.0.dp,
        defaultHeight = 384.0.dp,
        viewportWidth = 270.93f,
        viewportHeight = 101.6f
    ).apply {
        path(
            fill = SolidColor(MaterialTheme.colorScheme.background), pathFillType = NonZero
        ) {
            moveTo(0.0f, 93.14f)
            lineTo(270.93f, 93.14f)
            lineTo(270.93f, 101.6f)
            lineTo(0.0f, 101.6f)
            close()
        }
        path(
            fill = SolidColor(MaterialTheme.colorScheme.background),
            stroke = SolidColor(MaterialTheme.colorScheme.outline),
            strokeLineWidth = 1f,
            strokeLineCap = Round,
            strokeLineJoin = StrokeJoin.Companion.Round,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
        ) {
            moveToRelative(0.0f, 93.14f)
            curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
            curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
            curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
            curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
            curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
            curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
            reflectiveCurveToRelative(25.4f, -16.93f, 33.87f, -16.93f)
            curveToRelative(8.47f, 0.0f, 18.63f, 16.6f, 33.87f, 16.93f)
        }
    }.build()

    Icon(
        imageVector = wavyFill,
        contentDescription = "Wavy lines",
        tint = Color.Unspecified,
        modifier = modifier
    )
}

val wavyLinesExtended = ImageVector.Builder(
    name = "WavyLinesExtended",
    defaultWidth = 1024.dp,
    defaultHeight = 640.07.dp,
    viewportWidth = 270.93f,
    viewportHeight = 169.35f
).apply {
    listOf(160.88f, 127.01f, 93.14f, 59.27f, 25.4f).forEach { y ->
        path(
            fill = SolidColor(Color(0x00000000)),
            stroke = SolidColor(Color(0xFF000000)),
            strokeLineWidth = 1f,
            strokeLineCap = Round,
            strokeLineJoin = StrokeJoin.Companion.Round,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
        ) {
            moveToRelative(0.0f, y)
            curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
            curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
            curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
            curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
            curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
            curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
            reflectiveCurveToRelative(25.4f, -16.93f, 33.87f, -16.93f)
            curveToRelative(8.47f, 0.0f, 18.63f, 16.6f, 33.87f, 16.93f)
        }
    }
}.build()
