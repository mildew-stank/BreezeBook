package com.bsi.breezebook.viewmodels

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bsi.breezebook.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTheme(val displayName: String) {
    CALM_WATER("Calm Water"), HIGH_TIDE("High Tide"), DARK_NIGHT("Dark Night")
    //DYNAMIC("Dynamic")
}

data class SettingsUiState(
    val keepScreenOn: Boolean = false,
    val runInBackground: Boolean = false,
    val selectedTheme: AppTheme = AppTheme.CALM_WATER,
    val dashboardTutorialShown: Boolean = false,
    val logTutorialShown: Boolean = false,
    val chartTutorialShown: Boolean = false
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
        private val DASHBOARD_TUTORIAL_SHOWN_KEY = booleanPreferencesKey("dashboard_tutorial_shown")
        private val LOG_TUTORIAL_SHOWN_KEY = booleanPreferencesKey("log_tutorial_shown")
        private val CHART_TUTORIAL_SHOWN_KEY = booleanPreferencesKey("chart_tutorial_shown")
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
            val dashboardTutorialShown = preferences[DASHBOARD_TUTORIAL_SHOWN_KEY] == true
            val logTutorialShown = preferences[LOG_TUTORIAL_SHOWN_KEY] == true
            val chartTutorialShown = preferences[CHART_TUTORIAL_SHOWN_KEY] == true

            _uiState.update {
                it.copy(
                    keepScreenOn = keepScreenOn,
                    runInBackground = runInBackground,
                    selectedTheme = selectedTheme,
                    dashboardTutorialShown = dashboardTutorialShown,
                    logTutorialShown = logTutorialShown,
                    chartTutorialShown = chartTutorialShown
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

    fun dismissDashboardTutorial() {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { preferences ->
                preferences[DASHBOARD_TUTORIAL_SHOWN_KEY] = true
            }
            _uiState.update { it.copy(dashboardTutorialShown = true) }
        }
    }

    fun dismissLogTutorial() {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { preferences ->
                preferences[LOG_TUTORIAL_SHOWN_KEY] = true
            }
            _uiState.update { it.copy(logTutorialShown = true) }
        }
    }

    fun dismissChartTutorial() {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { preferences ->
                preferences[CHART_TUTORIAL_SHOWN_KEY] = true
            }
            _uiState.update { it.copy(chartTutorialShown = true) }
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