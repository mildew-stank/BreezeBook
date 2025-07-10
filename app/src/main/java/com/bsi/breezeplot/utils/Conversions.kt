package com.bsi.breezeplot.utils

import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

/**
 * Converts a decimal coordinate (latitude or longitude) to Degrees, Minutes, Seconds string.
 */
fun doubleToDMS(coordinate: Double, isLatitude: Boolean): String {
    val absoluteCoordinate = abs(coordinate)
    val degrees = absoluteCoordinate.toInt()
    val minutesDecimal = (absoluteCoordinate - degrees) * 60
    val minutes = minutesDecimal.toInt()
    val secondsDecimal = (minutesDecimal - minutes) * 60
    val seconds = round(secondsDecimal).toInt()
    val direction = if (isLatitude) {
        if (coordinate >= 0) 'N' else 'S'
    } else {
        if (coordinate >= 0) 'E' else 'W'
    }
    return String.format(
        Locale.getDefault(), "%d° %d' %d\" %c", degrees, minutes, seconds, direction
    )
}

/**
 * Converts speed from meters per second to knots.
 */
fun speedToKnots(speed: Float): Float {
    return speed * 1.94384f
}

/**
 * Converts distance from meters to nautical miles.
 */
fun distanceToNauticalMiles(distance: Float): Float {
    return distance * 0.000539957f
}
