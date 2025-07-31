package com.bsi.breezebook.viewmodels

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bsi.breezebook.dataStore
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
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
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow(GpsUiState())
    val uiState: StateFlow<GpsUiState> = _uiState.asStateFlow()

    private val _utcTime = MutableStateFlow(ZonedDateTime.now(ZoneOffset.UTC))
    val utcTime: StateFlow<ZonedDateTime> = _utcTime.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                updateLocationData(location)
            }
        }
    }

    init {
        viewModelScope.launch {
            while (true) {
                _utcTime.value = ZonedDateTime.now(ZoneOffset.UTC)
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    _uiState.update { it.copy(hasGpsAccuracy = false, hasClusterAccuracy = false) }
                }
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
    }

    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).apply {
            setMinUpdateIntervalMillis(5000)
            setWaitForAccurateLocation(true)
        }.build()

        if (ContextCompat.checkSelfPermission(
                getApplication(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        }
    }

    //fun stopLocationUpdates() {
    //    fusedLocationClient.removeLocationUpdates(locationCallback)
    //    _uiState.update { it.copy(hasGpsAccuracy = false, hasClusterAccuracy = false) }
    //}

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

    private fun updateLocationData(currentLocation: Location) {
        if (!currentLocation.hasAccuracy() && currentLocation.accuracy > 30.86f) { // 1 arc second
            _uiState.value = _uiState.value.copy(hasGpsAccuracy = false, hasClusterAccuracy = false)
            return
        }
        val inMotion = currentLocation.hasSpeed() && currentLocation.speed >= 0.51f
        val lastLocation = previousLocation
        var tripDistance = _uiState.value.tripDistance

        if (lastLocation != null) {
            val distanceMoved = lastLocation.distanceTo(currentLocation)
            val movementRecognitionThreshold = max(lastLocation.accuracy, currentLocation.accuracy)

            if (distanceMoved > movementRecognitionThreshold && distanceMoved >= 185.2f) { // 0.1NM
                tripDistance += distanceMoved
                previousLocation = currentLocation
            }
        } else {
            previousLocation = currentLocation
        }
        _uiState.value = _uiState.value.copy(
            hasGpsAccuracy = true,
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude,
            tripDistance = tripDistance,
            hasClusterAccuracy = inMotion, // 1kn
            speed = if (inMotion) currentLocation.speed else 0.0f,
            bearing = if (currentLocation.hasBearing() && inMotion) currentLocation.bearing else 0.0f
        )
    }

    fun resetDistance() {
        _uiState.value = _uiState.value.copy(tripDistance = 0.0f)
        previousLocation = null
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs.remove(DistancePrefs.TOTAL_DISTANCE)
                prefs.remove(DistancePrefs.LAST_LATITUDE)
                prefs.remove(DistancePrefs.LAST_LONGITUDE)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveTripData()
    }
}
