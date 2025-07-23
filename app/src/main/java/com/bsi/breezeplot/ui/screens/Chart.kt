package com.bsi.breezeplot.ui.screens

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
import com.bsi.breezeplot.ui.components.MainTemplate
import com.bsi.breezeplot.ui.components.PinDialog
import com.bsi.breezeplot.utilities.toRgbHexString
import com.bsi.breezeplot.viewmodels.AppTheme
import com.bsi.breezeplot.viewmodels.FormattedLogEntry
import com.bsi.breezeplot.viewmodels.GpsViewModel
import com.bsi.breezeplot.viewmodels.LogViewModel
import com.bsi.breezeplot.viewmodels.SettingsViewModel
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
    //val showInfoDialog = remember { mutableStateOf(false) }
    val logEntries by logViewModel.persistedLogEntries.collectAsState()
    var selectedEntry by remember { mutableStateOf<FormattedLogEntry?>(null) }
    val cameraPosition = rememberSaveable {
        mutableStateOf(
            CameraPosition(
                target = LatLng(gpsUiState.latitude, gpsUiState.longitude), zoom = 6.0
            )
        )
    }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(400)
        showContent = true
    }
    MainTemplate(showButtons = false)
    AnimatedVisibility(
        visible = showContent, enter = fadeIn(), exit = ExitTransition.None
    ) {
    // MapLibre requires ACCESS_NETWORK_STATE because of ConnectivityReceiver.java
    // Even if no network features are used removing it from manifest will crash to desktop
    MapLibre(
        // Can't seem to access foregroundLoadColor with ramani-maps so users get flash-banged
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        styleBuilder = Style.Builder().fromUri(getThemedMapStyle(settingsUiState.selectedTheme)),
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
                    onClick = { selectedEntry = logViewModel.getFormattedLogById(entry.id) },
                )
                Circle(
                    center = LatLng(entry.latitude, entry.longitude),
                    radius = 7.0f,
                    color = MaterialTheme.colorScheme.primary.toRgbHexString(),
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
        PinDialog(
            items = listOf(
                "Date" to entry.date,
                "Time" to entry.time,
                "Speed" to entry.speed,
                "Heading" to entry.bearing,
                "Latitude" to entry.latitude,
                "Longitude" to entry.longitude,
                "Segment" to entry.segmentDistance
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
