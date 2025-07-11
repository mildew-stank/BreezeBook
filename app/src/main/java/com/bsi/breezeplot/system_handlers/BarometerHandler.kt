package com.bsi.breezeplot.system_handlers

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

object BarometerPrefs {
    const val MAX_HISTORY_ITEMS = 6
    const val TOO_LATE_HOURS = 4L
    val INSTANT = List(MAX_HISTORY_ITEMS) { i -> longPreferencesKey("instant_$i") }
    val PRESSURE = List(MAX_HISTORY_ITEMS) { i -> floatPreferencesKey("pressure_$i") }
}

class BarometerViewModel(application: Application) : AndroidViewModel(application),
    SensorEventListener {
    private val dataStore: DataStore<Preferences> = application.dataStore
    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val pressureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    private val _currentPressure = MutableStateFlow(0f)
    val currentPressure = _currentPressure.asStateFlow()

    private val _hasBarometerAccuracy = MutableStateFlow(false)
    val hasBarometerAccuracy = _hasBarometerAccuracy.asStateFlow()

    private val _hasBarometer = MutableStateFlow(pressureSensor != null)
    val hasBarometer = _hasBarometer.asStateFlow()

    private val _pressureHistory = MutableStateFlow<List<Pair<Instant, Float>>>(emptyList())
    val pressureHistory = _pressureHistory.asStateFlow()

    val maxHistoryItems = BarometerPrefs.MAX_HISTORY_ITEMS
    val tooLateHours = BarometerPrefs.TOO_LATE_HOURS

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
            val tooOld = Duration.ofHours(BarometerPrefs.TOO_LATE_HOURS)
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

    //private fun startPeriodicLogging() {
    //    if (!_hasBarometer.value || loggingJob?.isActive == true) return
    //    loggingJob = scope.launch {
    //        // initial log
    //        while (_currentPressure.value == 0f && isActive) delay(1000L)
    //        addPressureReadingToHistory(_currentPressure.value)
    //        // periodic
    //        while (isActive) {
    //            delay(1000L * 60 * 30)
    //            addPressureReadingToHistory(_currentPressure.value)
    //        }
    //    }
    //}

    fun addPressureReadingToHistory(pressure: Float) {
        val list = _pressureHistory.value.toMutableList()
        if (list.size >= maxHistoryItems) list.removeAt(list.lastIndex)
        list.add(0, Instant.now() to pressure)
        _pressureHistory.value = list
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
        if (event.sensor.type == Sensor.TYPE_PRESSURE) {
            _currentPressure.value = event.values.first()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (sensor.type == Sensor.TYPE_PRESSURE) {
            _hasBarometerAccuracy.value = accuracy > 1
        }
    }

    override fun onCleared() {
        savePressureReadingHistory()
        super.onCleared()
    }
}
