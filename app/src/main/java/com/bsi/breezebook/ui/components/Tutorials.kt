package com.bsi.breezebook.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bsi.breezebook.viewmodels.SettingsViewModel

@Composable
fun DashboardTutorial(hasBarometer: Boolean, settingsViewModel: SettingsViewModel = viewModel()) {
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val isLoading by settingsViewModel.isLoading.collectAsState()

    if (isLoading || settingsUiState.dashboardTutorialShown) {
        return
    }
    val barometerTutorialText =
        if (hasBarometer) "Tap the barometer to open your pressure log. When the app is open and there hasn't been an entry in the last 30 minutes one will be added automatically.\n\n" else ""

    ConfirmationDialog(
        dialogText = "${barometerTutorialText}Tap the trip meter to reset it.\n\nLong press the screen to access settings.",
        confirmButtonText = "Close",
        dismissButtonText = "",
        onDismiss = { settingsViewModel.dismissDashboardTutorial() },
        onConfirm = { settingsViewModel.dismissDashboardTutorial() }
    )
}

@Composable
fun LogTutorial(settingsViewModel: SettingsViewModel = viewModel()) {
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val isLoading by settingsViewModel.isLoading.collectAsState()

    if (isLoading || settingsUiState.logTutorialShown) {
        return
    }
    ConfirmationDialog(
        dialogText = "Swipe log entries to delete.\n\nLong press the screen to export as CSV.",
        confirmButtonText = "Close",
        dismissButtonText = "",
        onDismiss = { settingsViewModel.dismissLogTutorial() },
        onConfirm = { settingsViewModel.dismissLogTutorial() }
    )
}

@Composable
fun ChartTutorial(settingsViewModel: SettingsViewModel = viewModel()) {
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val isLoading by settingsViewModel.isLoading.collectAsState()

    if (isLoading || settingsUiState.chartTutorialShown) {
        return
    }
    ConfirmationDialog(
        dialogText = "The bathymetric layer indicates a depth of less than 200m.\n\nGraticules are spaced 10° apart.",
        confirmButtonText = "Close",
        dismissButtonText = "",
        onDismiss = { settingsViewModel.dismissChartTutorial() },
        onConfirm = { settingsViewModel.dismissChartTutorial() }
    )
}
