package com.bsi.breezeplot.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bsi.breezeplot.DATE_FORMAT
import com.bsi.breezeplot.viewmodels.GpsViewModel
import com.bsi.breezeplot.viewmodels.LogEntry
import com.bsi.breezeplot.viewmodels.LogViewModel
import com.bsi.breezeplot.ui.components.ButtonCard
import com.bsi.breezeplot.ui.components.ConfirmationDialog
import com.bsi.breezeplot.ui.components.LogEntryCard
import com.bsi.breezeplot.ui.components.SwipeItem
import com.bsi.breezeplot.ui.graphics.wavyLines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Composable
fun LogScreen(gpsViewModel: GpsViewModel = viewModel(), logViewModel: LogViewModel = viewModel()) {
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
                    val success = logViewModel.exportLogbook(context, fileUri)
                    Toast.makeText(
                        context,
                        if (success) "Log exported." else "Failed to export log.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

    LogLayout(
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
fun LogLayout(
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
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                    //Text(
                    //    "No log entries.",
                    //    color = MaterialTheme.colorScheme.onBackground,
                    //    style = MaterialTheme.typography.bodySmall
                    //)
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
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(
                            items = logEntries, key = { _, entry -> entry.id }) { index, entry ->
                            // Entry item
                            Box(modifier = Modifier.animateItem()) {
                                SwipeItem(
                                    swipeAction = { onSwipeDismiss(entry) }) {
                                    LogEntryCard(
                                        entry = entry,
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp)
                                            .padding(top = 12.dp)
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    FilledWave(
                        Modifier
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
fun FilledWave(modifier: Modifier = Modifier) {
    val wavyFill = ImageVector.Builder(
        name = "WavyFill",
        defaultWidth = 1024.0.dp,
        defaultHeight = 384.0.dp,
        viewportWidth = 270.93f,
        viewportHeight = 101.6f
    ).apply {
        path(
            fill = SolidColor(MaterialTheme.colorScheme.background), pathFillType = NonZero
        ) {
            moveTo(0.0f, 93.14f)
            lineTo(270.93f, 93.14f)
            lineTo(270.93f, 101.6f)
            lineTo(0.0f, 101.6f)
            close()
        }
        path(
            fill = SolidColor(MaterialTheme.colorScheme.background),
            stroke = SolidColor(MaterialTheme.colorScheme.outline),
            strokeLineWidth = 1f,
            strokeLineCap = Round,
            strokeLineJoin = StrokeJoin.Companion.Round,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
        ) {
            moveToRelative(0.0f, 93.14f)
            curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
            curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
            curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
            curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
            curveToRelative(16.93f, 0.0f, 25.4f, -16.93f, 33.87f, -16.93f)
            curveToRelative(8.47f, 0.0f, 16.93f, 16.93f, 33.87f, 16.93f)
            reflectiveCurveToRelative(25.4f, -16.93f, 33.87f, -16.93f)
            curveToRelative(8.47f, 0.0f, 18.63f, 16.6f, 33.87f, 16.93f)
        }
    }.build()

    Icon(
        imageVector = wavyFill,
        contentDescription = "Wavy lines",
        tint = Color.Unspecified,
        modifier = modifier
    )
}
