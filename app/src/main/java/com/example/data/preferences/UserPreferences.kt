package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("stride_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_STEP_GOAL = "pref_step_goal"
        private const val KEY_STRIDE_LENGTH_CM = "pref_stride_length_cm"
        private const val KEY_THEME_MODE = "pref_theme_mode" // "SYSTEM", "LIGHT", "DARK"
        private const val KEY_BACKGROUND_SERVICE = "pref_background_service"
        private const val KEY_TRACKED_DATE = "pref_tracked_date"
        private const val KEY_BASE_SENSOR_VALUE = "pref_base_sensor_value"
        private const val KEY_LAST_RAW_SENSOR_VALUE = "pref_last_raw_sensor_value"
        private const val KEY_REBOOT_ACCRUED_STEPS = "pref_reboot_accrued_steps"
        private const val KEY_SIMULATION_STEPS_TODAY = "pref_sim_steps_today"
        private const val KEY_HAS_SEEDED_INITIAL_HISTORY = "pref_has_seeded_history"

        const val THEME_SYSTEM = "SYSTEM"
        const val THEME_LIGHT = "LIGHT"
        const val THEME_DARK = "DARK"
    }

    private val _stepGoalFlow = MutableStateFlow(getStepGoal())
    val stepGoalFlow: StateFlow<Int> = _stepGoalFlow.asStateFlow()

    private val _strideLengthFlow = MutableStateFlow(getStrideLengthCm())
    val strideLengthFlow: StateFlow<Int> = _strideLengthFlow.asStateFlow()

    private val _themeModeFlow = MutableStateFlow(getThemeMode())
    val themeModeFlow: StateFlow<String> = _themeModeFlow.asStateFlow()

    private val _backgroundServiceFlow = MutableStateFlow(isBackgroundServiceEnabled())
    val backgroundServiceFlow: StateFlow<Boolean> = _backgroundServiceFlow.asStateFlow()

    fun getStepGoal(): Int = prefs.getInt(KEY_STEP_GOAL, 8000)

    fun setStepGoal(goal: Int) {
        val safeGoal = goal.coerceIn(1000, 50000)
        prefs.edit().putInt(KEY_STEP_GOAL, safeGoal).apply()
        _stepGoalFlow.value = safeGoal
    }

    fun getStrideLengthCm(): Int = prefs.getInt(KEY_STRIDE_LENGTH_CM, 74)

    fun setStrideLengthCm(cm: Int) {
        val safeCm = cm.coerceIn(40, 120)
        prefs.edit().putInt(KEY_STRIDE_LENGTH_CM, safeCm).apply()
        _strideLengthFlow.value = safeCm
    }

    fun getThemeMode(): String = prefs.getString(KEY_THEME_MODE, THEME_LIGHT) ?: THEME_LIGHT

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeModeFlow.value = mode
    }

    fun isBackgroundServiceEnabled(): Boolean = prefs.getBoolean(KEY_BACKGROUND_SERVICE, true)

    fun setBackgroundServiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BACKGROUND_SERVICE, enabled).apply()
        _backgroundServiceFlow.value = enabled
    }

    // Hardware sensor tracking state persistence
    fun getTrackedDate(): String? = prefs.getString(KEY_TRACKED_DATE, null)

    fun setTrackedDate(date: String) {
        prefs.edit().putString(KEY_TRACKED_DATE, date).apply()
    }

    fun getBaseSensorValue(): Long = prefs.getLong(KEY_BASE_SENSOR_VALUE, -1L)

    fun setBaseSensorValue(value: Long) {
        prefs.edit().putLong(KEY_BASE_SENSOR_VALUE, value).apply()
    }

    fun getLastRawSensorValue(): Long = prefs.getLong(KEY_LAST_RAW_SENSOR_VALUE, -1L)

    fun setLastRawSensorValue(value: Long) {
        prefs.edit().putLong(KEY_LAST_RAW_SENSOR_VALUE, value).apply()
    }

    fun getRebootAccruedSteps(): Int = prefs.getInt(KEY_REBOOT_ACCRUED_STEPS, 0)

    fun setRebootAccruedSteps(steps: Int) {
        prefs.edit().putInt(KEY_REBOOT_ACCRUED_STEPS, steps).apply()
    }

    fun getSimulationStepsToday(): Int = prefs.getInt(KEY_SIMULATION_STEPS_TODAY, 0)

    fun setSimulationStepsToday(steps: Int) {
        prefs.edit().putInt(KEY_SIMULATION_STEPS_TODAY, steps).apply()
    }

    fun hasSeededInitialHistory(): Boolean = prefs.getBoolean(KEY_HAS_SEEDED_INITIAL_HISTORY, false)

    fun setHasSeededInitialHistory(seeded: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_SEEDED_INITIAL_HISTORY, seeded).apply()
    }
}
