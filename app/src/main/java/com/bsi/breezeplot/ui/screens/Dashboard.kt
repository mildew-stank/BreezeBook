package com.bsi.breezeplot.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import com.bsi.breezeplot.AppDestinations
import com.bsi.breezeplot.DATE_FORMAT
import com.bsi.breezeplot.TIME_FORMAT
import com.bsi.breezeplot.viewmodels.BarometerViewModel
import com.bsi.breezeplot.viewmodels.GpsViewModel
import com.bsi.breezeplot.ui.components.ButtonCard
import com.bsi.breezeplot.ui.components.ConfirmationDialog
import com.bsi.breezeplot.ui.components.PinDialog
//import com.bsi.breezeplot.ui.components.ThemeDialog
import com.bsi.breezeplot.ui.components.TitleCard
import com.bsi.breezeplot.ui.graphics.wavyLines
import com.bsi.breezeplot.utils.distanceToNauticalMiles
import com.bsi.breezeplot.utils.doubleToDMS
import com.bsi.breezeplot.utils.speedToKnots
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Locale

fun formatChronometer(color: Color, zonedDateTime: ZonedDateTime): AnnotatedString {
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

fun formatBarometer(locale: Locale, pressure: Float): AnnotatedString {
    val format = buildAnnotatedString {
        append(String.format(locale, "%.1f", pressure))
        withStyle(style = SpanStyle(fontSize = 18.sp)) { append("mb") }
    }
    return format
}

fun formatSpeed(locale: Locale, speed: Float): AnnotatedString {
    val format = buildAnnotatedString {
        append(String.format(locale, "%.1f", speedToKnots(speed)))
        withStyle(style = SpanStyle(fontSize = 18.sp)) { append("kn") }
    }
    return format
}

fun formatHeading(locale: Locale, heading: Float): String {
    return String.format(locale, "%.1f°", heading)
}

fun formatCoordinates(latitude: Double, longitude: Double): String {
    return "${doubleToDMS(latitude, true)}\n${doubleToDMS(longitude, false)}"

}

fun formatTripMeter(locale: Locale, trip: Float): AnnotatedString {
    val format = buildAnnotatedString {
        append(String.format(locale, "%.1f", distanceToNauticalMiles(trip)))
        withStyle(style = SpanStyle(fontSize = 18.sp)) { append("NM") }
    }
    return format
}

@Composable
fun DashboardScreen(
    navController: NavController,
    gpsViewModel: GpsViewModel = viewModel(),
    barometerViewModel: BarometerViewModel = viewModel()
) {
    val systemUTC by gpsViewModel.systemUtcTime.collectAsState()
    val speed by gpsViewModel.speed.collectAsState()
    val heading by gpsViewModel.bearing.collectAsState()
    val latitude by gpsViewModel.latitude.collectAsState()
    val longitude by gpsViewModel.longitude.collectAsState()
    val hasGpsAccuracy by gpsViewModel.hasGpsAccuracy.collectAsState()
    val hasClusterAccuracy by gpsViewModel.hasClusterAccuracy.collectAsState()
    val trip by gpsViewModel.tripDistance.collectAsState()
    val hasBarometer by barometerViewModel.hasBarometer.collectAsState()
    val hasBarometerAccuracy by barometerViewModel.hasBarometerAccuracy.collectAsState()
    val currentPressure by barometerViewModel.currentPressure.collectAsState()
    val pressureHistory by barometerViewModel.pressureHistory.collectAsState()
    val locale = Locale.getDefault()
    val dateColor = MaterialTheme.colorScheme.secondary
    val showBarometerDialog = remember { mutableStateOf(false) }
    val showTripDialog = remember { mutableStateOf(false) }
    val showSettingsDialog = remember { mutableStateOf(false) }

    // Main page
    DashboardLayout(
        navController,
        { showBarometerDialog.value = true },
        { showTripDialog.value = true },
        {
            //showSettingsDialog.value = true
            navController.navigate(AppDestinations.SETTINGS_ROUTE)
        },
        hasGpsAccuracy,
        hasClusterAccuracy,
        hasBarometer,
        hasBarometerAccuracy,
        formatChronometer(dateColor, systemUTC),
        formatBarometer(locale, currentPressure),
        formatSpeed(locale, speed),
        formatHeading(locale, heading),
        formatCoordinates(latitude, longitude),
        formatTripMeter(locale, trip)
    )
    // Dialog boxes
    if (showBarometerDialog.value) {
        val now = Instant.now()
        val tooLate = Duration.ofHours(barometerViewModel.tooLateHours)
        val itemsToDisplay = pressureHistory.filter { (instant, _) ->
            Duration.between(instant, now) < tooLate
        }.map { (instant, pressure) ->
            val age = Duration.between(instant, now)
            val hours = age.toHours()
            val minutes = age.minusHours(hours).toMinutes()
            val timeString = "-${hours}h ${minutes}m"
            val pressureString = "%.1fmb".format(pressure)

            timeString to pressureString
        }.toMutableList().apply {
            while (size < barometerViewModel.maxHistoryItems) {
                add("" to "")
            }
        }
        PinDialog(
            items = itemsToDisplay,
            onConfirm = { showBarometerDialog.value = false },
            onDismiss = { showBarometerDialog.value = false },
            onAction = {
                barometerViewModel.addPressureReadingToHistory(currentPressure)
                showBarometerDialog.value = false
            },
            actionButtonText = "Add entry"
        )
    } else if (showTripDialog.value) {
        ConfirmationDialog(
            dialogText = "Are you sure you want to reset trip distance?",
            onConfirm = {
                gpsViewModel.resetDistance()
                showTripDialog.value = false
            },
            onDismiss = { showTripDialog.value = false })
    } else if (showSettingsDialog.value) {
        // TODO: this stuff
        //ThemeDialog(onDismiss = { showSettingsDialog.value = false })
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

    Surface(
        Modifier
            .fillMaxSize()
            .combinedClickable(onLongClick = onPressSettings, onClick = {}),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier
                .systemBarsPadding()
                .padding(top = pad),
            verticalArrangement = Arrangement.SpaceBetween
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
                Column(
                    Modifier.padding(horizontal = pad),
                    verticalArrangement = Arrangement.spacedBy(pad)
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
                            "Speed",
                            speed,
                            modifier = Modifier.weight(1.0f),
                            dataColor = clusterDataColor
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
                        dataColor = gpsDataColor
                    )
                }
            }
            Row(Modifier.padding(pad), horizontalArrangement = Arrangement.spacedBy(pad)) {
                ButtonCard(
                    text = "Log",
                    onClick = { navController?.navigate(AppDestinations.LOG_ROUTE) },
                    modifier = Modifier.weight(1.0f)
                )
                ButtonCard(
                    text = "Chart",
                    onClick = { navController?.navigate(AppDestinations.CHART_ROUTE) },
                    modifier = Modifier.weight(1.0f)
                )
            }
        }
    }
}
