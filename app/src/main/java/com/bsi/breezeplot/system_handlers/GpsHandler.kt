package com.bsi.breezeplot.system_handlers

import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import java.time.ZonedDateTime

object DistancePrefs {
    val TOTAL_DISTANCE = floatPreferencesKey("total_distance")
    val LAST_LATITUDE = floatPreferencesKey("last_latitude")
    val LAST_LONGITUDE = floatPreferencesKey("last_longitude")
}

val Context.dataStore by preferencesDataStore(name = "settings")

class GpsViewModel(application: Application) : AndroidViewModel(application) {
    //private var previousAccuracy: Float? = null
    private var previousLocation: Location? = null
    private val dataStore = application.dataStore

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

    init {
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

    fun updateLocation(currentLocation: Location) {
        if (!currentLocation.hasAccuracy() || currentLocation.accuracy > 15.43f) { // 0.5 arc seconds
            Log.d("GpsViewModel", "FAIL1: Location accuracy insufficient: ${currentLocation.accuracy}")
            _hasGpsAccuracy.value = false
            return
        }
        Log.d("GpsViewModel", "PASS1: Location accuracy: ${currentLocation.accuracy}")
        val lastLocation = previousLocation
        _hasGpsAccuracy.value = true
        _latitude.value = currentLocation.latitude
        _longitude.value = currentLocation.longitude

        if (lastLocation == null) {
            Log.d("GpsViewModel", "First valid location received. Storing and awaiting next point.")
            previousLocation = currentLocation
            return
        }
        val distanceMoved = lastLocation.distanceTo(currentLocation)
        val lastAccuracy = lastLocation.accuracy
        val currentAccuracy = currentLocation.accuracy
        val movementRecognitionThreshold = (lastAccuracy + currentAccuracy) / 2.0f * 1.5f

        Log.d("GpsViewModel", "Distance: $distanceMoved, LastAccuracy: $lastAccuracy, CurrAccuracy: $currentAccuracy, MoveThreshold: $movementRecognitionThreshold")
        if (distanceMoved < movementRecognitionThreshold && distanceMoved < 2.57f) { // 1kn if / 5s tick
            Log.d("GpsViewModel", "FAIL2: Movement ($distanceMoved m) below threshold. Ignoring for cluster.")
            _hasClusterAccuracy.value = false
            // TODO: Consider only using a last known good location instead.
            previousLocation = currentLocation
            return
        }
        Log.d("GpsViewModel", "PASS2: Sufficient movement detected.")
        _hasClusterAccuracy.value = true
        _tripDistance.value += distanceMoved
        if (currentLocation.hasSpeed() && currentLocation.speed >= 0.51f) { // 1kn
            _speed.value = currentLocation.speed
        } else {//if (currentLocation.hasSpeed() && currentLocation.speed < MIN_SPEED_FOR_MOVEMENT_MPS && _speed.value != 0.0f) {
            // If device reports speed below threshold but we were moving, consider it stopped.
            _speed.value = 0.0f
        } //else if (_speed.value != 0.0f){
            // If no speed from device and we were moving, means we stopped.
            // This handles cases where speed drops to 0 and the device might not report it immediately.
           // _speed.value = 0.0f
        //}
        if (currentLocation.hasBearing() && _speed.value > 0.0f) {
            _bearing.value = currentLocation.bearing
        //} else if (_speed.value > 0.0f && distanceMoved > MIN_DISTANCE_FOR_RELIABLE_BEARING_METERS) {
            // Calculate bearing if not available from device but sufficient movement has occurred
            // Ensure lastLocation is not null (already checked, but good practice)
           // _bearing.value = lastLocation.bearingTo(currentLocation)
        } else {//if (_speed.value == 0.0f) {
             _bearing.value = 0.0f
        }
        _latitude.value = currentLocation.latitude
        _longitude.value = currentLocation.longitude
        previousLocation = currentLocation
    }

    /*
    fun updateLocation(location: Location) {
        // Overcome minimum accuracy level before updating coordinates
        if (!location.hasAccuracy() || location.accuracy > 15.43f) { // 0.5 arc seconds
            Log.d("GpsViewModel", "FAIL1 Location accuracy: ${location.accuracy}")
            _hasGpsAccuracy.value = false
            return
        }
        Log.d("GpsViewModel", "Location accuracy: ${location.accuracy}")
        val currentAccuracy = location.accuracy
        val filteredDistance = previousLocation?.distanceTo(location) ?: 0.0f

        _hasGpsAccuracy.value = true
        _latitude.value = location.latitude
        _longitude.value = location.longitude
        previousAccuracy = currentAccuracy
        previousLocation = location

        // Overcome the noise floor before updating speed, heading, and trip
        previousAccuracy?.let { previous ->
            val threshold = maxOf(previous, currentAccuracy) * 1.5f

            Log.d("GpsViewModel", "Threshold: $threshold, previous: $previous, current: $currentAccuracy")
            if (filteredDistance < threshold) {
                //_hasGpsAccuracy.value = false
                _hasClusterAccuracy.value = false
                return
            }
        } ?: run {
            // This is first pass so it can be fairly high
            if (filteredDistance < 10.28f) { // 4kn when divided by 5s update interval
                _hasClusterAccuracy.value = false
                return
            }
        }
        val isMoving = location.hasSpeed() && location.speed > 0.51f // 1kn
            _tripDistance.value += filteredDistance

        if (isMoving) {
            _hasClusterAccuracy.value = true
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
     */

    fun resetDistance() {
        _tripDistance.value = 0.0f
        saveTripData()
    }

    fun updateUtcTime() {
        _systemUtcTime.value = ZonedDateTime.now(ZoneOffset.UTC)
    }

    override fun onCleared() {
        saveTripData()
        super.onCleared()
    }
}
