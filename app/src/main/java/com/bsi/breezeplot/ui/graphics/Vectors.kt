package com.bsi.breezeplot.ui.graphics

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

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

val wavyFill = ImageVector.Builder(
    name = "WavyFill",
    defaultWidth = 1024.0.dp,
    defaultHeight = 384.0.dp,
    viewportWidth = 270.93f,
    viewportHeight = 101.6f
).apply {
    path(
        fill = SolidColor(Color(0xff254151)), pathFillType = NonZero
    ) {
        moveTo(0.0f, 93.14f)
        lineTo(270.93f, 93.14f)
        lineTo(270.93f, 101.6f)
        lineTo(0.0f, 101.6f)
        close()
    }
    path(
        fill = SolidColor(Color(0xff254151)),
        stroke = SolidColor(Color(0xff77b6b1)),
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
