package com.bsi.breezeplot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bsi.breezeplot.system_handlers.LogEntry
import com.bsi.breezeplot.ui.components.LogEntryCard
import com.bsi.breezeplot.ui.components.PinDialog
import com.bsi.breezeplot.ui.theme.BreezePlotTheme
import com.bsi.breezeplot.utils.doubleToDMS
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Preview
@Composable
fun DashboardPreview() {
    BreezePlotTheme {
        RenderDashboard()
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
    BreezePlotTheme { RenderLog(logEntries = mockList) }
}

@Preview()
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

@Preview()
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
            ),
            onConfirm = { null },
            onDismiss = { null }
        )
    }
}

@Preview()
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
            ),
            onConfirm = { null },
            onDismiss = { null },
            actionButtonText = "Add entry"
        )
    }
}
