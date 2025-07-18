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

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onStopped() {
            _uiState.value = _uiState.value.copy(hasGpsAccuracy = false)
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

            _uiState.value = _uiState.value.copy(
                tripDistance = prefs[DistancePrefs.TOTAL_DISTANCE] ?: 0f
            )

            if (lastLat != null && lastLon != null) {
                previousLocation = Location("lastSaved").apply {
                    latitude = lastLat.toDouble()
                    longitude = lastLon.toDouble()
                }
            }
        }
        Log.d(
            "GpsViewModel", "previousLocation init $previousLocation"
        )
    }

    fun saveTripData() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[DistancePrefs.TOTAL_DISTANCE] = _uiState.value.tripDistance
                previousLocation?.let {
                    prefs[DistancePrefs.LAST_LATITUDE] = it.latitude.toFloat()
                    prefs[DistancePrefs.LAST_LONGITUDE] = it.longitude.toFloat()
                }
            }
        }
    }

    fun updateLocationData(currentLocation: Location) {
        if (!currentLocation.hasAccuracy() || currentLocation.accuracy > 30.86f) { // 1 arc seconds
            _uiState.value = _uiState.value.copy(
                hasGpsAccuracy = false,
                hasClusterAccuracy = false
            )
            return
        }
        val lastLocation = previousLocation
        val newState = _uiState.value.copy(
            hasGpsAccuracy = true,
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude
        )

        if (lastLocation == null) {
            _uiState.value = newState.copy(hasClusterAccuracy = false)
            previousLocation = currentLocation
            return
        }
        val distanceMoved = lastLocation.distanceTo(currentLocation)
        val movementRecognitionThreshold = max(lastLocation.accuracy, currentLocation.accuracy)

        if (distanceMoved <= movementRecognitionThreshold) {
            _uiState.value = newState.copy(hasClusterAccuracy = false)
            return
        }
        if (distanceMoved >= 185.2f) { //0.1NM
            var tripDistance = newState.tripDistance

            tripDistance += distanceMoved
            _uiState.value = newState.copy(tripDistance = tripDistance)
            previousLocation = currentLocation
        }
        if (currentLocation.hasSpeed() && currentLocation.speed >= 0.51f) { // 1kn
            _uiState.value = newState.copy(
                hasClusterAccuracy = true,
                speed = currentLocation.speed,
                bearing = if (currentLocation.hasBearing()) currentLocation.bearing else 0.0f
            )
        } else {
            _uiState.value = newState.copy(
                hasClusterAccuracy = false,
                speed = 0.0f,
                bearing = 0.0f
            )
        }
    }

    fun resetDistance() {
        _uiState.value = _uiState.value.copy(tripDistance = 0.0f)
    }

    fun updateUtcTime() {
        _uiState.value = _uiState.value.copy(systemUtcTime = ZonedDateTime.now(ZoneOffset.UTC))
    }

    override fun onCleared() {
        saveTripData()
        super.onCleared()
    }
}
