// TODO:
//  Add setting to select theme.
//  Remove portrait restriction from AndroidManifest and do better.
//  Add setting to toggle keepScreenOn/run in background.
//  Add anchor alarm option to Chart. Would require run in background.
//  Add NMEA option to chart.
//  Convert geojson layers to PMTiles. Maybe. If more details are added or if slow devices struggle.
//  Make it easier to press a pin.
//  Add divider to Log.
//  Make it harder to accidentally flick dismiss a log card while scrolling.
//  Add a ? button on the chart to explain that the red area is a depth of less than 200 meters,
//   and that the gridlines are spaced 10 degrees apart.
//  Restore auto logging functionality. Maybe need JobScheduler and WorkManager or a foreground
//   process. 30min for barometer update but when out of focus have gps add to trip every 5min.

package com.bsi.breezeplot

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bsi.breezeplot.system_handlers.BarometerViewModel
import com.bsi.breezeplot.system_handlers.GpsViewModel
import com.bsi.breezeplot.system_handlers.LogEntry
import com.bsi.breezeplot.system_handlers.LogViewModel
import com.bsi.breezeplot.ui.components.ButtonCard
import com.bsi.breezeplot.ui.components.ConfirmationDialog
import com.bsi.breezeplot.ui.components.PinDialog
import com.bsi.breezeplot.ui.components.SwipeItem
import com.bsi.breezeplot.ui.components.TitleCard
import com.bsi.breezeplot.ui.graphics.wavyFill
import com.bsi.breezeplot.ui.graphics.wavyLines
import com.bsi.breezeplot.ui.theme.BreezePlotTheme
import com.bsi.breezeplot.utils.distanceToNauticalMiles
import com.bsi.breezeplot.utils.doubleToDMS
import com.bsi.breezeplot.utils.speedToKnots
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.Circle
import org.ramani.compose.LocationStyling
import org.ramani.compose.MapLibre
import org.ramani.compose.MapProperties
import org.ramani.compose.Polyline
import org.ramani.compose.UiSettings
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

object AppDestinations {
    const val DASHBOARD_ROUTE = "dashboard"
    const val LOG_ROUTE = "log"
    const val CHART_ROUTE = "chart"
}

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var gpsViewModel: GpsViewModel
    private lateinit var barometerViewModel: BarometerViewModel
    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val location = locationResult.lastLocation
            if (location != null) {
                gpsViewModel.updateLocation(location)
            }
        }
    }
    private val locationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).apply {
            setMinUpdateIntervalMillis(5000)
            setWaitForAccurateLocation(true)
        }.build()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Precise location permission is required", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        gpsViewModel = GpsViewModel(application)
        barometerViewModel = BarometerViewModel(application)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            // Have locationCallback definition here so it can access gpsViewModel
            LaunchedEffect(Unit) {
                startLocationUpdates()
            }
            LaunchedEffect(Unit) {
                while (true) {
                    gpsViewModel.updateUtcTime()
                    delay(1000)
                }
            }
            BreezePlotTheme {
                NavHost(
                    navController = navController,
                    startDestination = AppDestinations.DASHBOARD_ROUTE
                ) {
                    composable(AppDestinations.DASHBOARD_ROUTE) {
                        DisplayDashboard(
                            navController = navController,
                            gpsViewModel = gpsViewModel,
                            barometerViewModel = barometerViewModel
                        )
                    }
                    composable(AppDestinations.LOG_ROUTE) { DisplayLog(gpsViewModel = gpsViewModel) }
                    composable(AppDestinations.CHART_ROUTE) { DisplayChart(gpsViewModel = gpsViewModel) }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        gpsViewModel.saveTripData()
        barometerViewModel.savePressureReadingHistory()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

@Composable
fun DisplayDashboard(
    navController: NavController,
    gpsViewModel: GpsViewModel = viewModel(),
    barometerViewModel: BarometerViewModel = viewModel()
) {
    // Data
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
    // Misc
    val locale = Locale.getDefault()
    val dateColor = MaterialTheme.colorScheme.secondary
    val showBarometerDialog = remember { mutableStateOf(false) }
    val showTripDialog = remember { mutableStateOf(false) }
    // Strings for display
    val headingDisplay = remember(heading) { String.format(locale, "%.1f°", heading) }
    val coordinatesDisplay = remember(latitude, longitude) {
        doubleToDMS(latitude, true) + "\n" + doubleToDMS(longitude, false)
    }
    // AnnotatedStrings for display
    val chronometerDisplay = remember(systemUTC) {
        buildAnnotatedString {
            withStyle(style = SpanStyle(color = dateColor, fontSize = 18.sp)) {
                append(systemUTC.format(DATE_FORMAT) + "\n")
            }
            withStyle(style = SpanStyle(fontSize = 54.sp, fontFeatureSettings = "\"tnum\" on")) {
                append(systemUTC.format(TIME_FORMAT))
            }
        }
    }
    val pressureDisplay = remember(currentPressure) {
        buildAnnotatedString {
            append(String.format(locale, "%.1f", currentPressure))
            withStyle(style = SpanStyle(fontSize = 18.sp)) { append("mb") }
        }
    }
    val speedDisplay = remember(speed) {
        buildAnnotatedString {
            append(String.format(locale, "%.1f", speedToKnots(speed)))
            withStyle(style = SpanStyle(fontSize = 18.sp)) { append("kn") }
        }
    }
    val tripDisplay = remember(trip) {
        buildAnnotatedString {
            append(String.format(locale, "%.2f", distanceToNauticalMiles(trip)))
            withStyle(style = SpanStyle(fontSize = 18.sp)) { append("NM") }
        }
    }

    // Main page
    RenderDashboard(
        navController,
        { showBarometerDialog.value = true },
        { showTripDialog.value = true },
        hasGpsAccuracy,
        hasClusterAccuracy,
        hasBarometer,
        hasBarometerAccuracy,
        chronometerDisplay,
        pressureDisplay,
        speedDisplay,
        headingDisplay,
        coordinatesDisplay,
        tripDisplay
    )
    // Dialog boxes
    if (showBarometerDialog.value) {
        val now = Instant.now()
        val tooLate = Duration.ofHours(barometerViewModel.tooLateHours)
        val itemsToDisplay = pressureHistory.filter { (instant, _) ->
            Duration.between(instant, now) < tooLate
        }.map { (instant, pressure) ->
            //val timeString = SHORT_TIME_FORMAT.withZone(ZoneOffset.UTC).format(instant)
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
    }
}

@Composable
fun RenderDashboard(
    navController: NavController? = null,
    onPressBarometer: () -> Unit = {},
    onPressTripMeter: () -> Unit = {},
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
    val clusterDataColor = if (hasClusterAccuracy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val barometerDataColor =
        if (hasBarometerAccuracy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
                    tint = MaterialTheme.colorScheme.onBackground,
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

@Composable
fun DisplayLog(gpsViewModel: GpsViewModel = viewModel(), logViewModel: LogViewModel = viewModel()) {
    val latitude by gpsViewModel.latitude.collectAsState()
    val longitude by gpsViewModel.longitude.collectAsState()
    val speed by gpsViewModel.speed.collectAsState()
    val bearing by gpsViewModel.bearing.collectAsState()
    val showClearDialog = remember { mutableStateOf(false) }
    val showExportDialog = remember { mutableStateOf(false) }
    val logEntries by logViewModel.persistedLogEntries.collectAsState()
    val context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val logbookExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"), onResult = { uri: Uri? ->
            uri?.let { fileUri ->
                coroutineScope.launch {
                    val csvData = logViewModel.logToCSV()

                    if (csvData.isNotEmpty()) {
                        val success = logViewModel.tryWriteCsvToUri(fileUri, csvData, context)

                        if (success) {
                            Toast.makeText(context, "Log exported.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to export log.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(context, "Log empty.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

    RenderLog(
        { showExportDialog.value = true },
        { logViewModel.deleteLogEntry(it) },
        { logViewModel.addLogEntry(latitude, longitude, speed, bearing) },
        { showClearDialog.value = true },
        logEntries
    )
    if (showClearDialog.value) {
        ConfirmationDialog(
            dialogText = "Are you sure you want to clear all log entries?",
            onConfirm = {
                logViewModel.clearAllLogs()
                showClearDialog.value = false
            },
            onDismiss = { showClearDialog.value = false })
    } else if (showExportDialog.value) {
        ConfirmationDialog(dialogText = "Export logbook as CSV?", onConfirm = {
            val date = ZonedDateTime.now(ZoneOffset.UTC).format(DATE_FORMAT)

            showExportDialog.value = false
            logbookExportLauncher.launch("logbook_${date}.csv")
        }, onDismiss = { showExportDialog.value = false })
    }
}

@Composable
fun RenderLog(
    onPressExport: () -> Unit = {},
    onSwipeDismiss: (LogEntry) -> Unit = {},
    onPressAdd: () -> Unit = {},
    onPressClear: () -> Unit = {},
    logEntries: List<LogEntry> = emptyList()
) {
    var previousSize by remember { mutableIntStateOf(logEntries.size) }
    val listState = rememberLazyListState()

    LaunchedEffect(logEntries.size) {
        if (logEntries.size > previousSize) {
            listState.animateScrollToItem(0)
        }
        previousSize = logEntries.size
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(onLongClick = onPressExport, onClick = {}),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.systemBarsPadding()
        ) {
            if (logEntries.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    Icon(
                        imageVector = wavyLines,
                        contentDescription = "Wavy lines",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                    Text(
                        "No log entries.",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                // Entry list
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Icon(
                        imageVector = wavyLines,
                        contentDescription = "Wavy lines",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(
                            items = logEntries, key = { _, entry -> entry.id }) { index, entry ->
                            // Calculate segment distance
                            val segmentDistance = if (index < logEntries.size - 1) {
                                val previousEntryInTime = logEntries[index + 1]
                                val currentLocationObj = Location("currentLocationProvider").apply {
                                    this.latitude = entry.latitude
                                    this.longitude = entry.longitude
                                }
                                val previousLocationObj =
                                    Location("previousLocationProvider").apply {
                                        this.latitude = previousEntryInTime.latitude
                                        this.longitude = previousEntryInTime.longitude
                                    }
                                previousLocationObj.distanceTo(currentLocationObj)
                            } else {
                                0f
                            }
                            // Entry item
                            Box(modifier = Modifier.animateItem()) {
                                SwipeItem(
                                    entry = entry,
                                    segmentDistance = segmentDistance,
                                    swipeAction = { onSwipeDismiss(entry) })
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    Icon(
                        imageVector = wavyFill,
                        contentDescription = "Wavy lines",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                }
            }
            // Button row
            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ButtonCard(
                    text = "Add", onClick = onPressAdd, modifier = Modifier.weight(1.0f)
                )
                ButtonCard(
                    text = "Clear", onClick = onPressClear, modifier = Modifier.weight(1.0f)
                )
            }
        }
    }
}

@Composable
fun DisplayChart(
    gpsViewModel: GpsViewModel = viewModel(), logViewModel: LogViewModel = viewModel()
) {
    val latitude by gpsViewModel.latitude.collectAsState()
    val longitude by gpsViewModel.longitude.collectAsState()
    val logEntries by logViewModel.persistedLogEntries.collectAsState()
    var selectedEntry by remember { mutableStateOf<LogEntry?>(null) }
    val cameraPosition = rememberSaveable {
        mutableStateOf(CameraPosition(target = LatLng(latitude, longitude), zoom = 6.0))
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.systemBarsPadding()) {
            // MapLibre requires ACCESS_NETWORK_STATE because of ConnectivityReceiver.java
            // Even if no network features are used removing it from manifest will crash to desktop
            MapLibre(
                modifier = Modifier.fillMaxSize(),
                styleBuilder = Style.Builder().fromUri("asset://map_layers.json"),
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
                            radius = 7.0f,
                            color = "#e86b3b",
                            onClick = { selectedEntry = entry },
                        )
                    }
                    // Either ramani-maps or maplibre has a bug in the dashed line renderer
                    // Gets stuck solid when zooming way in, then way out or culled and re-rendered
                    Polyline(points = polylinePoints, color = "#ca4717", lineWidth = 2f)
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
                )
            }
        }
    }
}
