package com.bsi.breezeplot.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bsi.breezeplot.ui.components.ConfirmationDialog
import com.bsi.breezeplot.ui.components.LogEntryCard
import com.bsi.breezeplot.ui.components.MainTemplate
import com.bsi.breezeplot.ui.components.SwipeItem
import com.bsi.breezeplot.utilities.DATE_FORMAT
import com.bsi.breezeplot.utilities.distanceToNauticalMiles
import com.bsi.breezeplot.utilities.doubleToDMS
import com.bsi.breezeplot.utilities.speedToKnots
import com.bsi.breezeplot.viewmodels.FormattedLogEntry
import com.bsi.breezeplot.viewmodels.GpsViewModel
import com.bsi.breezeplot.viewmodels.LogEntry
import com.bsi.breezeplot.viewmodels.LogViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale

@Composable
private fun LogEntryItem(
    modifier: Modifier, entry: LogEntry, onSwipeDismiss: (LogEntry) -> Unit
) {
    Box(modifier = modifier) {
        SwipeItem(swipeAction = { onSwipeDismiss(entry) }) {
            LogEntryCard(
                entry = entry, modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
fun LogList(
    listState: LazyListState,
    logEntries: List<LogEntry>,
    visible: Boolean,
    onSwipeDismiss: (LogEntry) -> Unit
) {
    AnimatedVisibility(
        visible = visible, enter = fadeIn(), exit = ExitTransition.None
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = logEntries, key = { _, entry -> entry.id }) { index, entry ->
                LogEntryItem(Modifier.animateItem(), entry, onSwipeDismiss)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun LogScreen(gpsViewModel: GpsViewModel = viewModel(), logViewModel: LogViewModel = viewModel()) {
    val gpsUiState by gpsViewModel.uiState.collectAsState()
    val showClearDialog = remember { mutableStateOf(false) }
    val showExportDialog = remember { mutableStateOf(false) }
    val logEntries by logViewModel.persistedLogEntries.collectAsState()
    val formattedLogEntries by logViewModel.formattedLogEntries.collectAsState()
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
        gpsUiState.hasGpsAccuracy,
        { showExportDialog.value = true },
        { logViewModel.deleteLogEntry(it) },
        {
            logViewModel.addLogEntry(
                gpsUiState.latitude, gpsUiState.longitude, gpsUiState.speed, gpsUiState.bearing
            )
        },
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
    hasGpsAccuracy: Boolean = true,
    onPressExport: () -> Unit = {},
    onSwipeDismiss: (LogEntry) -> Unit = {},
    onPressAdd: () -> Unit = {},
    onPressClear: () -> Unit = {},
    logEntries: List<LogEntry> = emptyList()
) {
    val listState = rememberLazyListState()
    var previousSize by remember { mutableIntStateOf(logEntries.size) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200) // Let screen animation complete
        showContent = true
    }
    LaunchedEffect(logEntries.size) {
        if (logEntries.size > previousSize) {
            listState.animateScrollToItem(0)
        }
        previousSize = logEntries.size
    }
    MainTemplate(
        enableLongPress = true,
        maskBottomWave = true,
        buttonTextLeft = "Add",
        buttonTextRight = "Clear",
        buttonLeftEnabled = hasGpsAccuracy,
        onClickLeft = onPressAdd,
        onClickRight = onPressClear,
        onLongClick = onPressExport
    ) {
        LogList(listState, logEntries, showContent, onSwipeDismiss)
    }
}
