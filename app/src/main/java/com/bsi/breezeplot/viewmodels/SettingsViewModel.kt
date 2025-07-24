package com.bsi.breezeplot.viewmodels

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bsi.breezeplot.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTheme(val displayName: String) {
    CALM_WATER("Calm Water"),
    HIGH_TIDE("High Tide"),
    DARK_NIGHT("Dark Night")
    //DYNAMIC("Dynamic")
}

data class SettingsUiState(
    val keepScreenOn: Boolean = false,
    val runInBackground: Boolean = false,
    val selectedTheme: AppTheme = AppTheme.CALM_WATER
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    companion object {
        private val KEEP_SCREEN_ON_KEY = booleanPreferencesKey("keep_screen_on")
        private val RUN_IN_BACKGROUND_KEY = booleanPreferencesKey("run_in_background")
        private val SELECTED_THEME_KEY = stringPreferencesKey("selected_theme")
    }

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val preferences = getApplication<Application>().dataStore.data.first()
            val keepScreenOn = preferences[KEEP_SCREEN_ON_KEY] == true
            val runInBackground = preferences[RUN_IN_BACKGROUND_KEY] == true
            val themeString = preferences[SELECTED_THEME_KEY] ?: AppTheme.CALM_WATER.name
            val selectedTheme = try {
                AppTheme.valueOf(themeString)
            } catch (_: IllegalArgumentException) {
                AppTheme.CALM_WATER
            }

            _uiState.update {
                it.copy(
                    keepScreenOn = keepScreenOn,
                    runInBackground = runInBackground,
                    selectedTheme = selectedTheme
                )
            }
            _isLoading.value = false
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            val currentState = _uiState.value

            getApplication<Application>().dataStore.edit { preferences ->
                preferences[KEEP_SCREEN_ON_KEY] = currentState.keepScreenOn
                preferences[RUN_IN_BACKGROUND_KEY] = currentState.runInBackground
                preferences[SELECTED_THEME_KEY] = currentState.selectedTheme.name
            }
        }
    }

    fun setKeepScreenOn(keepOn: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(keepScreenOn = keepOn)
        }
    }

    fun setRunInBackground(runInBackground: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(runInBackground = runInBackground)
        }
    }

    fun setSelectedTheme(theme: AppTheme) {
        _uiState.update { currentState ->
            currentState.copy(selectedTheme = theme)
        }
    }

    override fun onCleared() {
        saveSettings()
        super.onCleared()
    }
}