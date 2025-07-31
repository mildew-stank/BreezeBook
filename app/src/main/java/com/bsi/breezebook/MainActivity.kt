// TODO:
//  Design landscape layout and remove portrait restriction from AndroidManifest.
//  Make it harder to accidentally flick dismiss a log card while scrolling.
//  Add an option to enable a foreground service. This would allow increased Trip Meter accuracy,
//   reliable Barometer auto-logging, and an anchor alarm.
//  Add divider to Log for separating trip segments.
//  Add NMEA support for local AIS to Chart.

package com.bsi.breezebook

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bsi.breezebook.ui.AppDestinations
import com.bsi.breezebook.ui.screens.ChartScreen
import com.bsi.breezebook.ui.screens.DashboardScreen
import com.bsi.breezebook.ui.screens.LogScreen
import com.bsi.breezebook.ui.screens.SettingsScreen
import com.bsi.breezebook.ui.theme.BreezePlotTheme
import com.bsi.breezebook.viewmodels.BarometerViewModel
import com.bsi.breezebook.viewmodels.GpsViewModel
import com.bsi.breezebook.viewmodels.LogViewModel
import com.bsi.breezebook.viewmodels.SettingsViewModel

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                gpsViewModel.startLocationUpdates()
            } else {
                Toast.makeText(
                    this, "Precise location permission is required", Toast.LENGTH_SHORT
                ).show()
            }
        }
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var gpsViewModel: GpsViewModel
    private lateinit var barometerViewModel: BarometerViewModel
    private lateinit var logViewModel: LogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        settingsViewModel = SettingsViewModel(application)
        gpsViewModel = GpsViewModel(application)
        barometerViewModel = BarometerViewModel(application)
        logViewModel = LogViewModel(application)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val settingsState by settingsViewModel.uiState.collectAsState()

            splashScreen.setKeepOnScreenCondition { settingsViewModel.isLoading.value }
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
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it }, animationSpec = tween(200)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it }, animationSpec = tween(200)
                        )
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it }, animationSpec = tween(200)
                        )
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it }, animationSpec = tween(200)
                        )
                    }) {
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

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onStart() {
        super.onStart()
        requestLocationPermission()
        gpsViewModel.startLocationUpdates()
        barometerViewModel.flagEarlyWake()
    }

    override fun onStop() {
        super.onStop()
        settingsViewModel.saveSettings()
        gpsViewModel.saveTripData()
        barometerViewModel.savePressureReadingHistory()
    }
}
