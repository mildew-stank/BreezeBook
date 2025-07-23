package com.bsi.breezeplot.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bsi.breezeplot.repository.AppDatabase
import com.bsi.breezeplot.repository.LogEntry
import com.bsi.breezeplot.repository.LogRepository
import com.bsi.breezeplot.utilities.DATE_FORMAT
import com.bsi.breezeplot.utilities.TIME_FORMAT
import com.bsi.breezeplot.utilities.distanceToNauticalMiles
import com.bsi.breezeplot.utilities.doubleToDMS
import com.bsi.breezeplot.utilities.speedToKnots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStreamWriter
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale

data class FormattedLogEntry(
    val id: String,
    val date: String,
    val time: String,
    val latitude: String,
    val longitude: String,
    val speed: String,
    val bearing: String,
    val segmentDistance: String
)

class LogViewModel(application: Application) : AndroidViewModel(application) {
    private val logRepository: LogRepository
    val persistedLogEntries: StateFlow<List<LogEntry>>
    val formattedLogEntries: StateFlow<List<FormattedLogEntry>>

    init {
        val logEntryDao = AppDatabase.getDatabase(application).logEntryDao()

        logRepository = LogRepository(logEntryDao)
        persistedLogEntries = logRepository.allLogEntries
            .map { entries -> calculateSegmentDistances(entries) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        formattedLogEntries = persistedLogEntries.map { entries ->
            entries.map { entry -> formatLogEntry(entry) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addLogEntry(
        latitudeVal: Double,
        longitudeVal: Double,
        speedVal: Float,
        bearingVal: Float,
        distanceVal: Float = 0f
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentTime = ZonedDateTime.now(ZoneOffset.UTC)
            val newEntry = LogEntry(
                latitude = latitudeVal,
                longitude = longitudeVal,
                speed = speedVal,
                bearing = bearingVal,
                distance = distanceVal,
                time = currentTime.format(TIME_FORMAT),
                date = currentTime.format(DATE_FORMAT),
            )

            logRepository.insert(newEntry)
        }
    }

    fun deleteLogEntry(logEntry: LogEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            logRepository.delete(logEntry)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            logRepository.clearAll()
        }
    }

    fun deleteLogById(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val logEntry = logRepository.getLogEntryById(id)

            if (logEntry != null) {
                logRepository.delete(logEntry)
            }
        }
    }

    fun getLogById(id: String): LogEntry? {
        return persistedLogEntries.value.find { it.id == id }
    }

    fun getFormattedLogById(id: String): FormattedLogEntry? {
        return formattedLogEntries.value.find { it.id == id }
    }

    fun formatLogEntry(entry: LogEntry): FormattedLogEntry {
        val locale = Locale.getDefault()

        return FormattedLogEntry(
            id = entry.id,
            date = entry.date,
            time = entry.time,
            latitude = doubleToDMS(entry.latitude, true),
            longitude = doubleToDMS(entry.longitude, false),
            speed = String.format(locale, "%.1fkn", speedToKnots(entry.speed)),
            bearing = String.format(locale, "%.1f°", entry.bearing),
            segmentDistance = String.format(locale, "%.2fNM", distanceToNauticalMiles(entry.distance))
        )
    }

    private fun calculateSegmentDistances(entries: List<LogEntry>): List<LogEntry> {
        entries.forEachIndexed { index, entry ->
            entry.distance = if (index < entries.size - 1) {
                val previousEntryInTime = entries[index + 1]
                val currentLocationObj = android.location.Location("currentLocationProvider").apply {
                    this.latitude = entry.latitude
                    this.longitude = entry.longitude
                }
                val previousLocationObj = android.location.Location("previousLocationProvider").apply {
                    this.latitude = previousEntryInTime.latitude
                    this.longitude = previousEntryInTime.longitude
                }
                previousLocationObj.distanceTo(currentLocationObj)
            } else {
                0f
            }
        }
        return entries
    }

    suspend fun exportLogbook(context: Context, uri: Uri): Boolean {
        val csvData = logRepository.logToCsv()

        return if (csvData.isNotEmpty()) {
            tryWriteCsvToUri(uri, csvData, context)
        } else {
            false
        }
    }

    suspend fun tryWriteCsvToUri(uri: Uri, csvData: String, context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(csvData)
                    }
                }
                true
            } catch (error: IOException) {
                error.printStackTrace()
                false
            }
        }
    }
}
