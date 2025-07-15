package com.bsi.breezeplot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bsi.breezeplot.ui.components.ButtonCard
import com.bsi.breezeplot.ui.components.RadioOptions
import com.bsi.breezeplot.ui.components.SwitchOption
import com.bsi.breezeplot.ui.components.TitledBorder
import com.bsi.breezeplot.ui.graphics.wavyLines
import com.bsi.breezeplot.ui.theme.BreezePlotTheme
import com.bsi.breezeplot.viewmodels.AppTheme
import com.bsi.breezeplot.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel = viewModel()) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val pad = 12.dp

    BreezePlotTheme (uiState.selectedTheme) {
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
                        //imageVector = generateWavyLines(5),
                        imageVector = wavyLines,
                        contentDescription = "Wavy lines",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                    Column(
                        Modifier.padding(horizontal = pad),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        TitledBorder(
                            title = "Power Management"
                        ) {
                            Column {
                                SwitchOption(
                                    text = "Keep screen on",
                                    defaultValue = uiState.keepScreenOn,
                                    onValueChange = { settingsViewModel.setKeepScreenOn(it) }
                                )
                                SwitchOption(
                                    text = "Run in background",
                                    defaultValue = uiState.runInBackground,
                                    onValueChange = { settingsViewModel.setRunInBackground(it) }
                                )
                            }
                        }
                        TitledBorder(
                            title = "Color Palette"
                        ) {
                            Column {
                                RadioOptions(
                                    options = AppTheme.entries.map { it.displayName },
                                    selectedOption = uiState.selectedTheme.displayName,
                                    onSelected = { selectedName ->
                                        val selectedTheme = AppTheme.entries.first { it.displayName == selectedName }
                                        settingsViewModel.setSelectedTheme(selectedTheme)
                                    }
                                )
                            }
                        }
                    }
                }
                // TODO: hiding the buttons like this for padding stinks. id like to have 2 more waves continue below
                Row(Modifier.padding(pad), horizontalArrangement = Arrangement.spacedBy(pad)) {
                    ButtonCard(
                        text = "",
                        onClick = { },
                        modifier = Modifier.weight(1.0f),
                        containerColor = Color.Transparent,
                        enabled = false
                    )
                    ButtonCard(
                        text = "",
                        onClick = { },
                        modifier = Modifier.weight(1.0f),
                        containerColor = Color.Transparent,
                        enabled = false
                    )
                }
            }
        }
    }
}