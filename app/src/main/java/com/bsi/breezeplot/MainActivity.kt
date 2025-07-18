// TODO:
//  Refactor hierarchy so each screen has one ViewModel that takes from gps/log/barometer classes.
//  Remove portrait restriction from AndroidManifest and make landscape mode.
//  Add divider to Log.
//  Make it harder to accidentally flick dismiss a log card while scrolling.
//  Add NMEA support to Chart.
//  Add an option to enable a foreground service. This would allow increased Trip Meter accuracy,
//   enable reliable Barometer auto-logging, and an anchor alarm.
//  Refactor screens to use MainTemplate.
//  +Trip Meter needs a lot more testing. Cut it back to Distance Logged if necessary.

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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bsi.breezeplot.ui.AppDestinations
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
            BreezePlotTheme(theme = settingsState.selectedTheme) {
                NavHost(
                    navController = navController,
                    startDestination = AppDestinations.DASHBOARD_ROUTE,
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
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
                            gpsViewModel = gpsViewModel,
                            logViewModel = logViewModel,
                            settingsViewModel = settingsViewModel
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
        settingsViewModel.saveSettings()
        gpsViewModel.saveTripData()
        barometerViewModel.savePressureReadingHistory()
    }
}
