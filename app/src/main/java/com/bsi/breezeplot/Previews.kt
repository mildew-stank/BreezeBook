package com.bsi.breezeplot

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bsi.breezeplot.ui.components.ButtonCard
import com.bsi.breezeplot.ui.components.LogEntryCard
import com.bsi.breezeplot.ui.components.PinDialog
import com.bsi.breezeplot.ui.components.RadioOptions
import com.bsi.breezeplot.ui.components.SettingsDialog
import com.bsi.breezeplot.ui.components.SwitchOption
import com.bsi.breezeplot.ui.components.TitledBorder
import com.bsi.breezeplot.ui.graphics.generateWavyLines
import com.bsi.breezeplot.ui.graphics.wavyLines
import com.bsi.breezeplot.ui.graphics.wavyLinesExtended
import com.bsi.breezeplot.ui.screens.DashboardLayout
import com.bsi.breezeplot.ui.screens.LogLayout
import com.bsi.breezeplot.ui.screens.SettingsScreen
import com.bsi.breezeplot.ui.theme.BreezePlotTheme
import com.bsi.breezeplot.utils.doubleToDMS
import com.bsi.breezeplot.viewmodels.LogEntry
import com.bsi.breezeplot.viewmodels.SettingsViewModel
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Preview
@Composable
fun DashboardPreview() {
    BreezePlotTheme {
        DashboardLayout()
    }
}

@Preview
@Composable
fun LogPreview() {
    val mockList = listOf(
        LogEntry(
            timestamp = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond(),
            latitude = 37.0522,
            longitude = -108.2437,
            speed = 15.5f,
            bearing = 180.0f,
            time = "17:32:22",
            date = "27/06/2025",
            id = "1"
        ), LogEntry(
            timestamp = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond(),
            latitude = 34.0522,
            longitude = -118.2437,
            speed = 15.5f,
            bearing = 180.0f,
            time = "17:32:22",
            date = "27/06/2025",
            id = "2"
        )
    )
    BreezePlotTheme { LogLayout(logEntries = mockList) }
}

@Preview
@Composable
fun LogEntryPreview() {
    BreezePlotTheme {
        val mockLogEntry = LogEntry(
            timestamp = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond(),
            latitude = 34.0522,
            longitude = -118.2437,
            speed = 15.5f,
            bearing = 180.0f,
            time = "17:32:22",
            date = "27/06/2025"
        )
        LogEntryCard(entry = mockLogEntry, segmentDistance = 140.0f)
    }
}

@Preview
@Composable
fun PinPreview() {
    BreezePlotTheme {
        PinDialog(
            items = listOf(
                "Date" to "12:34:56",
                "Time" to "01/02/2003",
                "Speed" to "8.4kn",
                "Heading" to "92.7°",
                "Latitude" to doubleToDMS(43.34634534, true),
                "Longitude" to doubleToDMS(33.345434, false)
            ), onConfirm = { null }, onDismiss = { null })
    }
}

@Preview
@Composable
fun BarometerLogPreview() {
    BreezePlotTheme {
        PinDialog(
            items = listOf(
                "21:54" to "925.6mb",
                "18:40" to "924.2mb",
                "18:02" to "924.1mb",
                "" to "",
                "" to "",
                "" to ""
            ), onConfirm = { null }, onDismiss = { null }, actionButtonText = "Add entry"
        )
    }
}

@Preview
@Composable
fun TogglePreview() {
    BreezePlotTheme {
        SettingsDialog(
            items = listOf(
                "Keep screen on" to true,
                "Run in background" to false,
            ),
            onConfirm = { null },
            onDismiss = { null },
        )
    }
}

//@Preview
//@Composable
//fun ThemeSelectPreview() {
//    BreezePlotTheme {
//        ThemeDialog(settingsViewModel = SettingsViewModel())
//    }
//}

//TODO: make this preview-able again same as others its the viewmodel
@Preview
@Composable
fun SettingsPreview() {
    BreezePlotTheme {
        SettingsScreen()
    }
}
