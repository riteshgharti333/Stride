package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DailyStepEntity
import com.example.data.preferences.UserPreferences
import com.example.sensor.SensorDiagnostics
import com.example.sensor.StepTrackerManager
import com.example.service.StepTrackingService
import com.example.ui.components.DayChartData
import com.example.widget.StrideAppWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

data class WeeklySummary(
    val weekRangeLabel: String,
    val totalSteps: Int,
    val averageStepsPerDay: Int,
    val mostActiveDayLabel: String,
    val mostActiveDaySteps: Int,
    val daysGoalMet: Int,
    val canGoForward: Boolean,
    val days: List<DayChartData>
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefs = UserPreferences(application)
    private val stepTracker = StepTrackerManager.getInstance(application)
    private val database = AppDatabase.getInstance(application)
    private val dao = database.dailyStepDao()

    val stepGoalFlow: StateFlow<Int> = userPrefs.stepGoalFlow
    val strideLengthFlow: StateFlow<Int> = userPrefs.strideLengthFlow
    val themeModeFlow: StateFlow<String> = userPrefs.themeModeFlow
    val backgroundServiceFlow: StateFlow<Boolean> = userPrefs.backgroundServiceFlow

    val todayStepsFlow: StateFlow<Int> = stepTracker.todayStepsFlow
    val sensorDiagnosticsFlow: StateFlow<SensorDiagnostics> = stepTracker.sensorDiagnosticsFlow

    // Historical records flow from Room
    val allHistoryFlow: StateFlow<List<DailyStepEntity>> = dao.getAllHistoryFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current displayed week start (Monday)
    private val _currentWeekStartFlow = MutableStateFlow(
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    )
    val currentWeekStartFlow: StateFlow<LocalDate> = _currentWeekStartFlow.asStateFlow()

    // Selected day index inside the weekly chart (0 = Mon .. 6 = Sun)
    private val _selectedDayIndexFlow = MutableStateFlow(
        // Default to today's day of week index (Monday = 0, Sunday = 6)
        (LocalDate.now().dayOfWeek.value - 1).coerceIn(0, 6)
    )
    val selectedDayIndexFlow: StateFlow<Int> = _selectedDayIndexFlow.asStateFlow()

    // Weekly summary reactive calculation
    val weeklySummaryFlow: StateFlow<WeeklySummary> = combine(
        currentWeekStartFlow,
        todayStepsFlow,
        stepGoalFlow,
        allHistoryFlow
    ) { weekStart, liveTodaySteps, goal, historyList ->
        computeWeeklySummary(weekStart, liveTodaySteps, goal, historyList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = computeWeeklySummary(
            _currentWeekStartFlow.value,
            todayStepsFlow.value,
            stepGoalFlow.value,
            emptyList()
        )
    )

    init {
        // Seed initial sample historical data for past 6 days if first launch
        viewModelScope.launch {
            if (!userPrefs.hasSeededInitialHistory()) {
                seedInitialHistory()
            }
        }

        // Start listening to hardware sensor
        stepTracker.startListening()

        // Update widget when steps or goal changes
        viewModelScope.launch {
            combine(todayStepsFlow, stepGoalFlow) { steps, goal ->
                Pair(steps, goal)
            }.collect { (steps, goal) ->
                StrideAppWidgetProvider.updateAllWidgets(getApplication(), steps, goal)
            }
        }
    }

    private suspend fun seedInitialHistory() {
        val today = LocalDate.now()
        val goal = userPrefs.getStepGoal()
        val stride = userPrefs.getStrideLengthCm()

        val sampleSteps = listOf(8420, 9150, 7800, 6920, 10340, 8100)
        val entities = mutableListOf<DailyStepEntity>()

        for (i in sampleSteps.indices) {
            val date = today.minusDays((sampleSteps.size - i).toLong())
            val steps = sampleSteps[i]
            val distanceKm = (steps * stride) / 100000f
            val calories = (steps * 0.04f).toInt()
            entities.add(
                DailyStepEntity(
                    date = date.toString(),
                    steps = steps,
                    goal = goal,
                    distanceKm = distanceKm,
                    calories = calories
                )
            )
        }
        dao.insertAll(entities)
        userPrefs.setHasSeededInitialHistory(true)
    }

    private fun computeWeeklySummary(
        weekStart: LocalDate,
        liveTodaySteps: Int,
        goal: Int,
        historyList: List<DailyStepEntity>
    ): WeeklySummary {
        val today = LocalDate.now()
        val historyMap = historyList.associateBy { it.date }
        val daysOfWeekLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        val dayChartDataList = mutableListOf<DayChartData>()
        var totalSteps = 0
        var daysGoalMet = 0
        var mostActiveDaySteps = 0
        var mostActiveDayLabel = "—"

        for (i in 0..6) {
            val date = weekStart.plusDays(i.toLong())
            val isToday = date.isEqual(today)
            val dateStr = date.toString()

            val steps = if (isToday) {
                liveTodaySteps
            } else {
                historyMap[dateStr]?.steps ?: 0
            }

            totalSteps += steps
            if (steps >= goal && goal > 0) {
                daysGoalMet++
            }

            if (steps > mostActiveDaySteps) {
                mostActiveDaySteps = steps
                mostActiveDayLabel = "${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} (${String.format("%,d", steps)})"
            }

            dayChartDataList.add(
                DayChartData(
                    date = date,
                    dayLabel = daysOfWeekLabels[i],
                    steps = steps,
                    isToday = isToday,
                    isSelected = false,
                    rawEntity = historyMap[dateStr]
                )
            )
        }

        val weekEnd = weekStart.plusDays(6)
        val rangeFmt = DateTimeFormatter.ofPattern("MMM d")
        val weekRangeLabel = "${weekStart.format(rangeFmt)} – ${weekEnd.format(rangeFmt)}"
        val averageSteps = totalSteps / 7
        val canGoForward = !weekStart.isAfter(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusDays(1))

        return WeeklySummary(
            weekRangeLabel = weekRangeLabel,
            totalSteps = totalSteps,
            averageStepsPerDay = averageSteps,
            mostActiveDayLabel = mostActiveDayLabel,
            mostActiveDaySteps = mostActiveDaySteps,
            daysGoalMet = daysGoalMet,
            canGoForward = canGoForward,
            days = dayChartDataList
        )
    }

    fun setSelectedDayIndex(index: Int) {
        _selectedDayIndexFlow.value = index.coerceIn(0, 6)
    }

    fun previousWeek() {
        _currentWeekStartFlow.value = _currentWeekStartFlow.value.minusWeeks(1)
    }

    fun nextWeek() {
        val currentWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        if (_currentWeekStartFlow.value.isBefore(currentWeek)) {
            _currentWeekStartFlow.value = _currentWeekStartFlow.value.plusWeeks(1)
        }
    }

    fun resetToCurrentWeek() {
        _currentWeekStartFlow.value = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        _selectedDayIndexFlow.value = (LocalDate.now().dayOfWeek.value - 1).coerceIn(0, 6)
    }

    fun setStepGoal(goal: Int) {
        userPrefs.setStepGoal(goal)
    }

    fun setStrideLengthCm(cm: Int) {
        userPrefs.setStrideLengthCm(cm)
    }

    fun setThemeMode(mode: String) {
        userPrefs.setThemeMode(mode)
    }

    fun toggleBackgroundService(enabled: Boolean) {
        userPrefs.setBackgroundServiceEnabled(enabled)
        val context = getApplication<Application>()
        if (enabled && StepTrackingService.hasRequiredPermissions(context)) {
            StepTrackingService.start(context)
        } else {
            StepTrackingService.stop(context)
        }
    }

    fun addManualSteps(count: Int) {
        stepTracker.addManualSteps(count)
    }

    fun resetTodaySteps() {
        stepTracker.resetTodaySteps()
    }
}
