// TODO:
//  Refactor hierarchy so screens have one ViewModel that takes from gps/log/barometer static classes.
//  Remove portrait restriction from AndroidManifest and do better.
//  +Add an option to enable a foreground service. This would allow increased Trip Meter accuracy,
//   reliable Barometer auto-logging, and an anchor alarm.
//  Add divider to Log.
//  Make it harder to accidentally flick dismiss a log card while scrolling.
//  Add NMEA support to Chart.
//  Add a ? button on the chart to explain that the red area is a depth of less than 200 meters,
//   and that the gridlines are spaced 10 degrees apart.
//  ++Set proper theme colors in dark_night_map.json
//  +?Add confirmation dialog before going to settings from dashboard long press
//  +Lots of cleanup
//  +Trip meter and such needs tested again

package com.bsi.breezeplot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bsi.breezeplot.viewmodels.BarometerViewModel
import com.bsi.breezeplot.viewmodels.GpsViewModel
import com.bsi.breezeplot.viewmodels.LogViewModel
import com.bsi.breezeplot.ui.screens.ChartScreen
import com.bsi.breezeplot.ui.screens.DashboardScreen
import com.bsi.breezeplot.ui.screens.LogScreen
import com.bsi.breezeplot.ui.screens.SettingsScreen
import com.bsi.breezeplot.ui.theme.BreezePlotTheme
import com.bsi.breezeplot.viewmodels.SettingsViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.time.format.DateTimeFormatter

val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

object AppDestinations {
    const val DASHBOARD_ROUTE = "dashboard"
    const val LOG_ROUTE = "log"
    const val CHART_ROUTE = "chart"
    const val SETTINGS_ROUTE = "settings"
}

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                requestLocationUpdates()
            } else {
                Toast.makeText(
                    this, "Precise location permission is required", Toast.LENGTH_SHORT
                ).show()
            }
        }
    private lateinit var gpsViewModel: GpsViewModel
    private lateinit var logViewModel: LogViewModel
    private lateinit var barometerViewModel: BarometerViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpsViewModel = GpsViewModel(application)
        logViewModel = LogViewModel(application)
        barometerViewModel = BarometerViewModel(application)
        settingsViewModel = SettingsViewModel(application)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val settingsState by settingsViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                requestLocationUpdates()
            }
            LaunchedEffect(settingsState.keepScreenOn) {
                if (settingsState.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
            BreezePlotTheme (theme = settingsState.selectedTheme) {
                NavHost(
                    navController = navController,
                    startDestination = AppDestinations.DASHBOARD_ROUTE
                ) {
                    composable(AppDestinations.DASHBOARD_ROUTE) {
                        DashboardScreen(
                            navController = navController,
                            gpsViewModel = gpsViewModel,
                            barometerViewModel = barometerViewModel
                        )
                    }
                    composable(AppDestinations.LOG_ROUTE) {
                        LogScreen(
                            gpsViewModel = gpsViewModel, logViewModel = logViewModel
                        )
                    }
                    composable(AppDestinations.CHART_ROUTE) {
                        ChartScreen(
                            gpsViewModel = gpsViewModel, logViewModel = logViewModel, settingsViewModel = settingsViewModel
                        )
                    }
                    composable(AppDestinations.SETTINGS_ROUTE) {
                        SettingsScreen(settingsViewModel = settingsViewModel)
                    }
                }
            }
        }
    }

    private fun requestLocationUpdates() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        var locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                if (location != null) {
                    gpsViewModel.updateLocationData(location)
                }
            }
        }
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).apply {
            setMinUpdateIntervalMillis(5000)
            setWaitForAccurateLocation(true)
        }.build()

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

    override fun onStop() {
        super.onStop()
        //gpsViewModel.saveTripData()//
        //barometerViewModel.savePressureReadingHistory()//
    }
}
