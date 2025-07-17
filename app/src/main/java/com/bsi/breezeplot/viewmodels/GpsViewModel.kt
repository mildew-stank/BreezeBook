package com.bsi.breezeplot.viewmodels

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bsi.breezeplot.dataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.max

object DistancePrefs {
    val TOTAL_DISTANCE = floatPreferencesKey("total_distance")
    val LAST_LATITUDE = floatPreferencesKey("last_latitude")
    val LAST_LONGITUDE = floatPreferencesKey("last_longitude")
}

data class GpsUiState(
    val hasGpsAccuracy: Boolean = false,
    val hasClusterAccuracy: Boolean = false,
    val systemUtcTime: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speed: Float = 0.0f,
    val bearing: Float = 0.0f,
    val tripDistance: Float = 0.0f
)

class GpsViewModel(application: Application) : AndroidViewModel(application) {
    private var previousLocation: Location? = null
    private val dataStore: DataStore<Preferences> = application.dataStore
    private val locationManager =
        application.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _uiState = MutableStateFlow(GpsUiState())
    val uiState: StateFlow<GpsUiState> = _uiState.asStateFlow()

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

    private val _tripDistance = MutableStateFlow(0.0f)
    val tripDistance: StateFlow<Float> = _tripDistance.asStateFlow()

    private val _hasClusterAccuracy = MutableStateFlow(false)
    val hasClusterAccuracy: StateFlow<Boolean> = _hasClusterAccuracy.asStateFlow()

    private val _systemUtcTime = MutableStateFlow(ZonedDateTime.now(ZoneOffset.UTC))
    val systemUtcTime: StateFlow<ZonedDateTime> = _systemUtcTime.asStateFlow()

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onStopped() {
            _hasGpsAccuracy.value = false
        }
    }

    init {
        if (ContextCompat.checkSelfPermission(
                application, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.registerGnssStatusCallback(
                gnssStatusCallback, Handler(Looper.getMainLooper())
            )
        }
        viewModelScope.launch {
            while (true) {
                updateUtcTime()
                delay(1000)
            }
        }
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val lastLat = prefs[DistancePrefs.LAST_LATITUDE]
            val lastLon = prefs[DistancePrefs.LAST_LONGITUDE]

            _tripDistance.value = prefs[DistancePrefs.TOTAL_DISTANCE] ?: 0f
            if (lastLat != null && lastLon != null) {
                previousLocation = Location("lastSaved").apply {
                    latitude = lastLat.toDouble()
                    longitude = lastLon.toDouble()
                }
            }
        }
        Log.d(
            "GpsViewModel", "previousLocation init $previousLocation"
        ) // TODO: more logging and testing of gps filter
    }

    fun saveTripData() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[DistancePrefs.TOTAL_DISTANCE] = _tripDistance.value
                previousLocation?.let {
                    prefs[DistancePrefs.LAST_LATITUDE] = it.latitude.toFloat()
                    prefs[DistancePrefs.LAST_LONGITUDE] = it.longitude.toFloat()
                }
            }
        }
    }

    fun updateLocationData(currentLocation: Location) {
        if (!currentLocation.hasAccuracy() || currentLocation.accuracy > 15.43f) { // 0.5 arc seconds
            _hasGpsAccuracy.value = false
            _hasClusterAccuracy.value = false
            return
        }
        val lastLocation = previousLocation

        _hasGpsAccuracy.value = true
        _latitude.value = currentLocation.latitude
        _longitude.value = currentLocation.longitude
        if (lastLocation == null) {
            _hasClusterAccuracy.value = false
            previousLocation = currentLocation
            return
        }
        val distanceMoved = lastLocation.distanceTo(currentLocation)
        val movementRecognitionThreshold = max(lastLocation.accuracy, currentLocation.accuracy)

        if (distanceMoved <= movementRecognitionThreshold) {
            _hasClusterAccuracy.value = false
            return
        }
        val filteredDistanceMoved = lastLocation.distanceTo(currentLocation)

        if (filteredDistanceMoved >= 185.2) { //0.1NM
            _tripDistance.value += distanceMoved
            previousLocation = currentLocation
        }
        if (currentLocation.hasSpeed() && currentLocation.speed >= 0.51f) { // 1kn
            _hasClusterAccuracy.value = true
            _speed.value = currentLocation.speed
        } else {
            _hasClusterAccuracy.value = false
            _speed.value = 0.0f
            _bearing.value = 0.0f
            return
        }
        if (currentLocation.hasBearing()) {
            _bearing.value = currentLocation.bearing
        }
    }

    fun resetDistance() {
        _tripDistance.value = 0.0f
    }

    fun updateUtcTime() {
        _systemUtcTime.value = ZonedDateTime.now(ZoneOffset.UTC)
    }

    override fun onCleared() {
        saveTripData()
        super.onCleared()
    }
}
