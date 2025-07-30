package com.bsi.breezebook.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bsi.breezebook.ui.AppDestinations
import com.bsi.breezebook.ui.components.ConfirmationDialog
import com.bsi.breezebook.ui.components.DashboardTutorial
import com.bsi.breezebook.ui.components.MainTemplate
import com.bsi.breezebook.ui.components.PinDialog
import com.bsi.breezebook.ui.components.TitleCard
import com.bsi.breezebook.utilities.DATE_FORMAT
import com.bsi.breezebook.utilities.TIME_FORMAT
import com.bsi.breezebook.utilities.distanceToNauticalMiles
import com.bsi.breezebook.utilities.doubleToDMS
import com.bsi.breezebook.utilities.speedToKnots
import com.bsi.breezebook.viewmodels.BarometerViewModel
import com.bsi.breezebook.viewmodels.GpsViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Locale

private enum class ActiveDialog {
    BAROMETER, TRIP, SETTINGS
}

private fun formatChronometer(color: Color, zonedDateTime: ZonedDateTime): AnnotatedString {
    val format = buildAnnotatedString {
        withStyle(style = SpanStyle(color = color, fontSize = 18.sp)) {
            append(zonedDateTime.format(DATE_FORMAT) + "\n")
        }
        withStyle(style = SpanStyle(fontSize = 54.sp, fontFeatureSettings = "\"tnum\" on")) {
            append(zonedDateTime.format(TIME_FORMAT))
        }
    }

    return format
}

private fun formatBarometer(pressure: Float): AnnotatedString {
    val format = buildAnnotatedString {
        append(String.format(Locale.getDefault(), "%.1f", pressure))
        withStyle(style = SpanStyle(fontSize = 18.sp)) { append("mb") }
    }

    return format
}

private fun formatSpeed(speed: Float): AnnotatedString {
    val format = buildAnnotatedString {
        append(String.format(Locale.getDefault(), "%.1f", speedToKnots(speed)))
        withStyle(style = SpanStyle(fontSize = 18.sp)) { append("kn") }
    }

    return format
}

private fun formatHeading(heading: Float): String {
    return String.format(Locale.getDefault(), "%.1f°", heading)
}

private fun formatCoordinates(latitude: Double, longitude: Double): String {
    return "${doubleToDMS(latitude, true)}\n${doubleToDMS(longitude, false)}"

}

private fun formatTripMeter(trip: Float): AnnotatedString {
    val format = buildAnnotatedString {
        append(String.format(Locale.getDefault(), "%.1f", distanceToNauticalMiles(trip)))
        withStyle(style = SpanStyle(fontSize = 18.sp)) { append("NM") }
    }

    return format
}

private fun formatPressureHistory(
    pressureHistory: List<Pair<Instant, Float>>, expirationHour: Long, maxItems: Int
): List<Pair<String, String>> {
    val now = Instant.now()
    val expiry = Duration.ofHours(expirationHour)
    val itemsToDisplay = pressureHistory.filter { (instant, _) ->
        Duration.between(instant, now) < expiry
    }.map { (instant, pressure) ->
        val age = Duration.between(instant, now)
        val hours = age.toHours()
        val minutes = age.minusHours(hours).toMinutes()
        val timeString = "-${hours}h ${minutes}m"
        val pressureString = "%.1fmb".format(pressure)

        timeString to pressureString
    }.toMutableList().apply {
        while (size < maxItems) {
            add("" to "")
        }
    }
    return itemsToDisplay
}

@Composable
fun DashboardScreen(
    navController: NavController,
    gpsViewModel: GpsViewModel = viewModel(),
    barometerViewModel: BarometerViewModel = viewModel()
) {
    val gpsUiState by gpsViewModel.uiState.collectAsState()
    val utcTime by gpsViewModel.utcTime.collectAsState()
    val hasBarometer by barometerViewModel.hasBarometer.collectAsState()
    val hasBarometerAccuracy by barometerViewModel.hasAccuracy.collectAsState()
    val currentPressure by barometerViewModel.currentPressure.collectAsState()
    val pressureHistory by barometerViewModel.pressureHistory.collectAsState()
    val dateColor = MaterialTheme.colorScheme.secondary
    val activeDialog = remember { mutableStateOf<ActiveDialog?>(null) }

    // Main page
    DashboardLayout(
        navController,
        onPressBarometer = { activeDialog.value = ActiveDialog.BAROMETER },
        onPressTripMeter = { activeDialog.value = ActiveDialog.TRIP },
        onPressSettings = { activeDialog.value = ActiveDialog.SETTINGS },
        gpsUiState.hasGpsAccuracy,
        gpsUiState.hasClusterAccuracy,
        hasBarometer,
        hasBarometerAccuracy,
        formatChronometer(dateColor, utcTime),
        formatBarometer(currentPressure),
        formatSpeed(gpsUiState.speed),
        formatHeading(gpsUiState.bearing),
        formatCoordinates(gpsUiState.latitude, gpsUiState.longitude),
        formatTripMeter(gpsUiState.tripDistance)
    )
    // Dialog boxes
    when (activeDialog.value) {
        ActiveDialog.BAROMETER -> PinDialog(
            items = formatPressureHistory(
                pressureHistory, barometerViewModel.expiryHours, barometerViewModel.maxHistoryItems
            ),
            onConfirm = { activeDialog.value = null },
            onDismiss = { activeDialog.value = null },
            onAction = {
                barometerViewModel.addPressureReadingToHistory(currentPressure)
                activeDialog.value = null
            },
            actionButtonText = "Add entry"
        )

        ActiveDialog.TRIP -> ConfirmationDialog(
            dialogText = "Are you sure you want to reset trip distance?",
            onConfirm = {
                gpsViewModel.resetDistance()
                activeDialog.value = null
            },
            onDismiss = { activeDialog.value = null })

        ActiveDialog.SETTINGS -> ConfirmationDialog(
            dialogText = "Open settings menu?",
            onConfirm = {
                navController.navigate(AppDestinations.SETTINGS_ROUTE)
                activeDialog.value = null
            },
            onDismiss = { activeDialog.value = null })

        null -> {}
    }
}

@Composable
fun DashboardLayout(
    navController: NavController? = null,
    onPressBarometer: () -> Unit = {},
    onPressTripMeter: () -> Unit = {},
    onPressSettings: () -> Unit = {},
    hasGpsAccuracy: Boolean = true,
    hasClusterAccuracy: Boolean = true,
    hasBarometer: Boolean = true,
    hasBarometerAccuracy: Boolean = true,
    chronometer: AnnotatedString = AnnotatedString("06-07-2025\n17:08:52"),
    barometer: AnnotatedString = AnnotatedString("1035"),
    speed: AnnotatedString = AnnotatedString("11.3"),
    heading: String = "192.3",
    coordinates: String = "121 53\" 23' N\n9 38\" 56' W",
    trip: AnnotatedString = AnnotatedString("12,534.78")
) {
    val pad = 12.dp
    val gpsDataColor =
        if (hasGpsAccuracy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val clusterDataColor =
        if (hasClusterAccuracy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val barometerDataColor =
        if (hasBarometerAccuracy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface

    MainTemplate(
        enableLongPress = true,
        buttonTextLeft = "Log",
        buttonTextRight = "Chart",
        onClickLeft = { navController?.navigate(AppDestinations.LOG_ROUTE) },
        onClickRight = { navController?.navigate(AppDestinations.CHART_ROUTE) },
        onLongClick = onPressSettings
    ) {
        Column(
            Modifier.padding(horizontal = pad), verticalArrangement = Arrangement.spacedBy(pad)
        ) {
            TitleCard("Universal Time", chronometer)
            if (hasBarometer) {
                TitleCard(
                    "Barometer",
                    barometer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(pad))
                        .combinedClickable(onClick = onPressBarometer),
                    dataColor = barometerDataColor
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(pad)) {
                TitleCard(
                    "Speed", speed, modifier = Modifier.weight(1.0f), dataColor = clusterDataColor
                )
                TitleCard(
                    "Heading",
                    heading,
                    modifier = Modifier.weight(1.0f),
                    dataColor = clusterDataColor
                )
            }
            TitleCard("Coordinates", coordinates, dataColor = gpsDataColor)
            TitleCard(
                "Trip Meter",
                trip,
                modifier = Modifier
                    .clip(RoundedCornerShape(pad))
                    .combinedClickable(onClick = onPressTripMeter),
                dataColor = MaterialTheme.colorScheme.primary
            )
        }
        DashboardTutorial(hasBarometer)
    }
}
