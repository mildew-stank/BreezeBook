package com.bsi.breezeplot.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bsi.breezeplot.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

object BarometerPrefs {
    const val MAX_HISTORY_ITEMS = 6
    const val EXPIRY_HOURS = 6L
    const val AUTO_LOG_MINUTES = 1L
    val INSTANT = List(MAX_HISTORY_ITEMS) { i -> longPreferencesKey("instant_$i") }
    val PRESSURE = List(MAX_HISTORY_ITEMS) { i -> floatPreferencesKey("pressure_$i") }
}

class BarometerViewModel(application: Application) : AndroidViewModel(application),
    SensorEventListener {
    private val dataStore: DataStore<Preferences> = application.dataStore
    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val pressureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
    private var isEarlyWake = true

    private val _currentPressure = MutableStateFlow(0f)
    val currentPressure = _currentPressure.asStateFlow()

    private val _hasAccuracy = MutableStateFlow(false)
    val hasAccuracy = _hasAccuracy.asStateFlow()

    private val _hasBarometer = MutableStateFlow(pressureSensor != null)
    val hasBarometer = _hasBarometer.asStateFlow()

    private val _pressureHistory = MutableStateFlow<List<Pair<Instant, Float>>>(emptyList())
    val pressureHistory = _pressureHistory.asStateFlow()

    val maxHistoryItems = BarometerPrefs.MAX_HISTORY_ITEMS
    val expiryHours = BarometerPrefs.EXPIRY_HOURS

    init {
        if (_hasBarometer.value) {
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        loadPressureHistory()
    }

    private fun loadPressureHistory() {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val now = Instant.now()
            val tooOld = Duration.ofHours(BarometerPrefs.EXPIRY_HOURS)
            val list = mutableListOf<Pair<Instant, Float>>()
            for (i in 0 until BarometerPrefs.MAX_HISTORY_ITEMS) {
                prefs[BarometerPrefs.INSTANT[i]]?.let { timestamp ->
                    prefs[BarometerPrefs.PRESSURE[i]]?.let { pressure ->
                        Instant.ofEpochSecond(timestamp)
                            .takeIf { Duration.between(it, now) < tooOld }
                            ?.let { list += it to pressure }
                    }
                }
            }
            _pressureHistory.value = list
        }
    }

    fun addPressureReadingToHistory(pressure: Float) {
        val list = _pressureHistory.value.toMutableList()

        if (list.size >= maxHistoryItems) {
            list.removeAt(list.lastIndex)
        }
        list.add(0, Instant.now() to pressure)
        _pressureHistory.value = list
    }

    fun tryAutoLogPressureReadingToHistory(pressure: Float, timestamp: Long): Boolean {
        if (isEarlyWake) {
            return false
        }
        val lastLog = _pressureHistory.value.firstOrNull()?.first
        val now = Instant.now()
        val autoLogTimer = Duration.ofMinutes(BarometerPrefs.AUTO_LOG_MINUTES)

        if (lastLog == null || Duration.between(lastLog, now) >= autoLogTimer) {
            val ageMillis = (SystemClock.elapsedRealtimeNanos() - timestamp) / 1_000_000L

            if (!_hasAccuracy.value || ageMillis > 1000) {
                return false
            }
            addPressureReadingToHistory(pressure)
            return true
        }
        return false
    }

    fun flagEarlyWake(durationMillis: Long = 1000L) {
        isEarlyWake = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(durationMillis)
            isEarlyWake = false
        }
    }

    fun savePressureReadingHistory() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                _pressureHistory.value.take(BarometerPrefs.MAX_HISTORY_ITEMS)
                    .forEachIndexed { i, (instant, pressure) ->
                        prefs[BarometerPrefs.INSTANT[i]] = instant.epochSecond
                        prefs[BarometerPrefs.PRESSURE[i]] = pressure
                    }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_PRESSURE) {
            return
        }
        _currentPressure.value = event.values.first()
        tryAutoLogPressureReadingToHistory(_currentPressure.value, event.timestamp)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (sensor.type != Sensor.TYPE_PRESSURE) {
            return
        }
        _hasAccuracy.value = accuracy > 1
    }

    override fun onCleared() {
        super.onCleared()
        savePressureReadingHistory()
    }
}
