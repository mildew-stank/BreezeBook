package com.bsi.breezebook.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bsi.breezebook.repository.LogEntry
import com.bsi.breezebook.ui.components.MainTemplate
import com.bsi.breezebook.ui.components.PinDialog
import com.bsi.breezebook.utilities.toRgbHexString
import com.bsi.breezebook.viewmodels.AppTheme
import com.bsi.breezebook.viewmodels.GpsViewModel
import com.bsi.breezebook.viewmodels.LogViewModel
import com.bsi.breezebook.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.Circle
import org.ramani.compose.LocationStyling
import org.ramani.compose.MapLibre
import org.ramani.compose.MapProperties
import org.ramani.compose.Polyline
import org.ramani.compose.UiSettings

@Composable
fun ChartScreen(
    gpsViewModel: GpsViewModel = viewModel(),
    logViewModel: LogViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val gpsUiState by gpsViewModel.uiState.collectAsState()
    val logEntries by logViewModel.persistedLogEntries.collectAsState()
    var selectedEntry by remember { mutableStateOf<LogEntry?>(null) }
    var showContent by remember { mutableStateOf(false) }
    //val showInfoDialog = remember { mutableStateOf(false) }
    val cameraPosition = rememberSaveable {
        mutableStateOf(
            CameraPosition(
                target = LatLng(gpsUiState.latitude, gpsUiState.longitude), zoom = 6.0
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }
    MainTemplate(showButtons = false)
    AnimatedVisibility(
        visible = showContent, enter = fadeIn(), exit = ExitTransition.None
    ) {
        // MapLibre requires ACCESS_NETWORK_STATE because of ConnectivityReceiver.java
        // Even if no network features are used removing it from manifest will crash to desktop
        MapLibre(
            // Can't seem to set foregroundLoadColor in time with ramani-maps to prevent flash-bang
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            styleBuilder = Style.Builder()
                .fromUri(getThemedMapStyle(settingsUiState.selectedTheme)),
            cameraPosition = cameraPosition.value,
            locationStyling = LocationStyling(
                foregroundTintColor = MaterialTheme.colorScheme.primary.toArgb(),
                backgroundTintColor = MaterialTheme.colorScheme.secondary.toArgb(),
            ),
            uiSettings = UiSettings(
                isAttributionEnabled = false,
                rotateGesturesEnabled = false,
                tiltGesturesEnabled = false,
                isLogoEnabled = false,
            ),
            properties = MapProperties(maxZoom = 12.0)
        ) {
            if (logEntries.isNotEmpty()) {
                val polylinePoints: List<LatLng> =
                    logEntries.map { entry -> LatLng(entry.latitude, entry.longitude) }
                logEntries.forEach { entry ->
                    Circle(
                        center = LatLng(entry.latitude, entry.longitude),
                        radius = 20.0f,
                        opacity = 0.0f,
                        onClick = { selectedEntry = entry }
                    )
                    Circle(
                        center = LatLng(entry.latitude, entry.longitude),
                        radius = 7.0f,
                        color = MaterialTheme.colorScheme.primary.toRgbHexString()
                    )
                }
                // Either ramani-maps or maplibre has a bug in the dashed line renderer
                // Gets stuck solid when zooming way in, then way out or culled and re-rendered
                Polyline(
                    points = polylinePoints,
                    color = MaterialTheme.colorScheme.tertiary.toRgbHexString(),
                    lineWidth = 2f
                )
            }
        }
    }
    selectedEntry?.let { entry ->
        val formattedEntry = logViewModel.formatLogEntry(entry)

        PinDialog(
            items = listOf(
                "Date" to formattedEntry.date,
                "Time" to formattedEntry.time,
                "Speed" to formattedEntry.speed,
                "Heading" to formattedEntry.bearing,
                "Latitude" to formattedEntry.latitude,
                "Longitude" to formattedEntry.longitude,
                "Segment" to formattedEntry.segmentDistance
            ),
            onConfirm = { selectedEntry = null },
            onDismiss = { selectedEntry = null },
            actionButtonText = "Delete",
            onAction = {
                logViewModel.deleteLogById(entry.id)
                selectedEntry = null
            })
    }
    //FloatingActionButton(
    //    onClick = { showInfoDialog.value = true },
    //    modifier = Modifier
    //        .align(Alignment.BottomEnd)
    //        .padding(16.dp),
    //    containerColor = MaterialTheme.colorScheme.outline,
    //    contentColor = MaterialTheme.colorScheme.background
    //) {
    //    Text(
    //        text = "?",
    //        style = MaterialTheme.typography.labelMedium,
    //        fontWeight = FontWeight.Bold
    //    )
    //}
    //if (showInfoDialog.value) {
    //    ConfirmationDialog(
    //        dialogText = "Bathymetric layer indicates a depth of less than 200m.\n\nGraticules are spaced 10° apart.",
    //        onConfirm = { showInfoDialog.value = false },
    //        onDismiss = { showInfoDialog.value = false },
    //        dismissButtonText = "",
    //        confirmButtonText = "Close"
    //    )
    //}
}

@Composable
fun getThemedMapStyle(currentTheme: AppTheme): String {
    return when (currentTheme) {
        AppTheme.CALM_WATER -> "asset://calm_water_map.json"
        AppTheme.HIGH_TIDE -> "asset://high_tide_map.json"
        AppTheme.DARK_NIGHT -> "asset://dark_night_map.json"
        //AppTheme.DYNAMIC -> "asset://calm_water_map.json"
    }
}
