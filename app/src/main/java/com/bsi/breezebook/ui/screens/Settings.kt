package com.bsi.breezebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bsi.breezebook.ui.components.MainTemplate
import com.bsi.breezebook.ui.components.RadioOptions
import com.bsi.breezebook.ui.components.SwitchOption
import com.bsi.breezebook.ui.components.TitledBorder
import com.bsi.breezebook.ui.theme.BreezePlotTheme
import com.bsi.breezebook.viewmodels.AppTheme
import com.bsi.breezebook.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel = viewModel()) {
    val uiState by settingsViewModel.uiState.collectAsState()

    SettingsLayout(
        keepScreenOn = uiState.keepScreenOn,
        runInBackground = uiState.runInBackground,
        appTheme = uiState.selectedTheme,
        onKeepScreenOnChange = { settingsViewModel.setKeepScreenOn(!uiState.keepScreenOn) },
        onRunInBackgroundChange = { settingsViewModel.setRunInBackground(!uiState.runInBackground) },
        onThemeChange = { settingsViewModel.setSelectedTheme(it) })
}

@Composable
fun SettingsLayout(
    keepScreenOn: Boolean = false,
    runInBackground: Boolean = false,
    appTheme: AppTheme = AppTheme.CALM_WATER,
    onKeepScreenOnChange: (Boolean) -> Unit = {},
    onRunInBackgroundChange: (Boolean) -> Unit = {},
    onThemeChange: (AppTheme) -> Unit = {}
) {
    val pad = 12.dp

    BreezePlotTheme(appTheme) {
        MainTemplate(showButtons = false) {
            Column(
                Modifier.padding(horizontal = pad), verticalArrangement = Arrangement.spacedBy(pad)
            ) {
                TitledBorder(
                    title = "Power Management"
                ) {
                    Column {
                        SwitchOption(
                            text = "Keep screen on",
                            defaultValue = keepScreenOn,
                            onValueChange = { onKeepScreenOnChange(it) })
                        //SwitchOption(
                        //    text = "Run in background",
                        //    defaultValue = runInBackground,
                        //    onValueChange = {
                        //        onRunInBackgroundChange(it)
                        //    })
                    }
                }
                TitledBorder(
                    title = "Color Palette"
                ) {
                    Column {
                        RadioOptions(
                            options = AppTheme.entries.map { it.displayName },
                            selectedOption = appTheme.displayName,
                            onSelected = { selectedName ->
                                val selectedTheme =
                                    AppTheme.entries.first { it.displayName == selectedName }
                                onThemeChange(selectedTheme)
                            })
                    }
                }
            }
        }
    }
}