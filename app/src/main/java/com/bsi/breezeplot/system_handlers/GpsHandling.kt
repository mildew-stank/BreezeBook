package com.bsi.breezeplot.system_handlers

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

object DistancePrefs {
    val TOTAL_DISTANCE = floatPreferencesKey("total_distance")
    val LAST_LATITUDE = floatPreferencesKey("last_latitude")
    val LAST_LONGITUDE = floatPreferencesKey("last_longitude")
}

object BarometerPrefs {
    const val MAX_HISTORY_ITEMS = 6
    const val TOO_LATE_HOURS = 4L
    //const val TOO_EARLY_MINUTES = 15L
    val INSTANT = List(MAX_HISTORY_ITEMS) { i -> longPreferencesKey("instant_$i") }
    val PRESSURE = List(MAX_HISTORY_ITEMS) { i -> floatPreferencesKey("pressure_$i") }
}

val Context.dataStore by preferencesDataStore(name = "settings")

class GpsViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    private var previousLocation: Location? = null
    private val dataStore = application.dataStore
    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val pressureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    private val _latitude = MutableStateFlow(0.0)
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(0.0)
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    private val _hasGpsAccuracy = MutableStateFlow(false)
    val hasGpsAccuracy: StateFlow<Boolean> = _hasGpsAccuracy.asStateFlow()

    private val _speed = MutableStateFlow(0.0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()

    private val _bearing = MutableStateFlow(0.0f)
    val bearing: StateFlow<Float> = _bearing.asStateFlow()

    private val _distance = MutableStateFlow(0.0f)
    val distance: StateFlow<Float> = _distance.asStateFlow()

    private val _systemUtcTime = MutableStateFlow(ZonedDateTime.now(ZoneOffset.UTC))
    val systemUtcTime: StateFlow<ZonedDateTime> = _systemUtcTime.asStateFlow()

    private val _currentPressure = MutableStateFlow(0.0f)
    val currentPressure: StateFlow<Float> = _currentPressure.asStateFlow()

    private val _hasBarometerAccuracy = MutableStateFlow(false)
    val hasBarometerAccuracy: StateFlow<Boolean> = _hasBarometerAccuracy.asStateFlow()

    private val _hasBarometer = MutableStateFlow(pressureSensor != null)
    val hasBarometer: StateFlow<Boolean> = _hasBarometer.asStateFlow()

    private val _pressureHistory = MutableStateFlow<List<Pair<Instant, Float>>>(emptyList())
    val pressureHistory: StateFlow<List<Pair<Instant, Float>>> = _pressureHistory.asStateFlow()

    val maxHistoryItems = 6
    val logIntervalMillis = 1000L * 60 * 30 // 30 minutes
    private var pressureLoggingJob: Job? = null

    init {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val lastLat = prefs[DistancePrefs.LAST_LATITUDE]
            val lastLon = prefs[DistancePrefs.LAST_LONGITUDE]
            val lastPressureHistoryList = mutableListOf<Pair<Instant, Float>>()
            val now = Instant.now()
            val tooOld = Duration.ofHours(BarometerPrefs.TOO_LATE_HOURS)
            //val tooNew = java.time.Duration.ofMinutes(BarometerPrefs.TOO_EARLY_MINUTES)

            _distance.value = prefs[DistancePrefs.TOTAL_DISTANCE] ?: 0f
            if (lastLat != null && lastLon != null) {
                previousLocation = Location("lastSaved").apply {
                    latitude = lastLat.toDouble()
                    longitude = lastLon.toDouble()
                }
            }
            if (_hasBarometer.value) {
                startBarometerListener()
                //startPeriodicPressureLogging()
            }
            for (i in 0 until BarometerPrefs.MAX_HISTORY_ITEMS) {
                val instant = prefs[BarometerPrefs.INSTANT[i]]
                val pressure = prefs[BarometerPrefs.PRESSURE[i]]

                if (instant != null && pressure != null) {
                    val then = Instant.ofEpochSecond(instant)
                    val age = Duration.between(then, now)
                    if (age < tooOld) {
                        lastPressureHistoryList.add(then to pressure)
                    }
                }
            }
            _pressureHistory.value = lastPressureHistoryList.take(BarometerPrefs.MAX_HISTORY_ITEMS)
        }
    }

    fun saveTripData() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[DistancePrefs.TOTAL_DISTANCE] = _distance.value
                previousLocation?.let {
                    prefs[DistancePrefs.LAST_LATITUDE] = it.latitude.toFloat()
                    prefs[DistancePrefs.LAST_LONGITUDE] = it.longitude.toFloat()
                }
            }
        }
    }

    fun updateLocation(location: Location) {
        if (location.hasAccuracy() && location.accuracy > 10.28f) { // 20 arc seconds
            _hasGpsAccuracy.value = false
            return
        }
        val filteredDistance = previousLocation?.distanceTo(location) ?: 0.0f
        val isMoving = location.hasSpeed() && location.speed > 0.51f // 1kn

        _hasGpsAccuracy.value = true
        if (isMoving || filteredDistance > 2.55f) { // 5kn, 1kn with tick rate of 5s
            _distance.value += filteredDistance
        }
        previousLocation = location
        _latitude.value = location.latitude
        _longitude.value = location.longitude
        if (isMoving) {
            _speed.value = location.speed
        } else {
            _speed.value = 0.0f
        }
        if (isMoving && location.hasBearing()) {
            _bearing.value = location.bearing
        } else {
            _bearing.value = 0.0f
        }
    }

    fun resetDistance() {
        _distance.value = 0.0f
    }

    fun updateUtcTime() {
        _systemUtcTime.value = ZonedDateTime.now(ZoneOffset.UTC)
    }

    private fun startBarometerListener() {
        pressureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun startPeriodicPressureLogging() {
        // TODO: logs stale data when phone is asleep, perhaps on wake.
        //  foreground service is starting to look appealing.
        //  JobScheduler + WorkManager might be acceptable.
        //  barometer reading every 30min and gps reading every 5min for trip meter.
        if (!_hasBarometer.value || pressureLoggingJob?.isActive == true) return

        pressureLoggingJob = viewModelScope.launch {
            var initialPressureLogged = false

            // Initial Log attempt
            while (!initialPressureLogged && isActive) {
                val currentPressureValue = _currentPressure.value

                if (currentPressureValue != 0.0f) {
                    addPressureReadingToHistory(currentPressureValue)
                    initialPressureLogged = true
                } else {
                    if (!isActive) break
                    delay(1000L)
                }
            }
            // Periodic Logging
            while (isActive) {
                delay(logIntervalMillis)
                if (!isActive) break
                val pressureToLog = _currentPressure.value

                if (pressureToLog != 0.0f) {
                    addPressureReadingToHistory(pressureToLog)
                }
            }
        }
    }

    fun addPressureReadingToHistory(pressure: Float) {
        val currentList = _pressureHistory.value.toMutableList()

        if (currentList.size >= maxHistoryItems) {
            currentList.removeAt(currentList.size - 1)
        }
        currentList.add(0, Instant.now() to pressure)
        _pressureHistory.value = currentList.toList()
    }

    fun savePressureHistory() {
        viewModelScope.launch {
            val recordedHistory = _pressureHistory.value.take(BarometerPrefs.MAX_HISTORY_ITEMS)
            dataStore.edit { prefs ->
                // Clear old entries
                for (i in 0 until BarometerPrefs.MAX_HISTORY_ITEMS) {
                    prefs.remove(BarometerPrefs.INSTANT[i])
                    prefs.remove(BarometerPrefs.PRESSURE[i])
                }
                // Save current entries
                recordedHistory.forEachIndexed { index, pair ->
                    prefs[BarometerPrefs.INSTANT[index]] = pair.first.epochSecond
                    prefs[BarometerPrefs.PRESSURE[index]] = pair.second
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
            _currentPressure.value = event.values[0]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _hasBarometerAccuracy.value = accuracy > 1
    }

    override fun onCleared() {
        saveTripData()
        savePressureHistory()
        super.onCleared()
    }
}
