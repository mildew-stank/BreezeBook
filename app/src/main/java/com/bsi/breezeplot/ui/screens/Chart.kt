package com.bsi.breezeplot.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bsi.breezeplot.viewmodels.GpsViewModel
import com.bsi.breezeplot.viewmodels.LogEntry
import com.bsi.breezeplot.viewmodels.LogViewModel
import com.bsi.breezeplot.ui.components.PinDialog
import com.bsi.breezeplot.utils.doubleToDMS
import com.bsi.breezeplot.utils.speedToKnots
import com.bsi.breezeplot.utils.toRgbHexString
import com.bsi.breezeplot.viewmodels.AppTheme
import com.bsi.breezeplot.viewmodels.SettingsViewModel
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.Circle
import org.ramani.compose.LocationStyling
import org.ramani.compose.MapLibre
import org.ramani.compose.MapProperties
import org.ramani.compose.Polyline
import org.ramani.compose.UiSettings
import java.util.Locale


@Composable
fun ChartScreen(
    gpsViewModel: GpsViewModel = viewModel(), logViewModel: LogViewModel = viewModel(), settingsViewModel: SettingsViewModel = viewModel()
) {
    val settingsState by settingsViewModel.uiState.collectAsState()
    val latitude by gpsViewModel.latitude.collectAsState()
    val longitude by gpsViewModel.longitude.collectAsState()
    val logEntries by logViewModel.persistedLogEntries.collectAsState()
    var selectedEntry by remember { mutableStateOf<LogEntry?>(null) }
    val cameraPosition = rememberSaveable {
        mutableStateOf(CameraPosition(target = LatLng(latitude, longitude), zoom = 6.0))
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.systemBarsPadding()) {
            Log.w("CHART", getThemedMapStyle(settingsState.selectedTheme))
            // MapLibre requires ACCESS_NETWORK_STATE because of ConnectivityReceiver.java
            // Even if no network features are used removing it from manifest will crash to desktop
            MapLibre(
                modifier = Modifier.fillMaxSize(),
                styleBuilder = Style.Builder().fromUri(getThemedMapStyle(settingsState.selectedTheme)),
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
                            onClick = { selectedEntry = entry },
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
                        color = MaterialTheme.colorScheme.onPrimary.toRgbHexString(),
                        lineWidth = 2f
                    )
                }
            }
            selectedEntry?.let { entry ->
                PinDialog(
                    items = listOf(
                    "Date" to entry.date,
                    "Time" to entry.time,
                    "Speed" to String.format(
                        Locale.getDefault(), "%.1fkn", speedToKnots(entry.speed)
                    ),
                    "Heading" to String.format(Locale.getDefault(), "%.1f°", entry.bearing),
                    "Latitude" to doubleToDMS(entry.latitude, true),
                    "Longitude" to doubleToDMS(entry.longitude, false)
                ),
                    onConfirm = { selectedEntry = null },
                    onDismiss = { selectedEntry = null },
                    actionButtonText = "Delete",
                    onAction = {
                        logViewModel.deleteLogEntry(entry)
                        selectedEntry = null
                    })
            }
        }
    }
}

@Composable
fun getThemedMapStyle(currentTheme: AppTheme): String {
    return when (currentTheme) {
        AppTheme.CALM_WATER -> "asset://calm_water_map.json"
        AppTheme.HIGH_TIDE -> "asset://high_tide_map.json"
        AppTheme.DARK_NIGHT -> "asset://dark_night_map.json"
    }
}
