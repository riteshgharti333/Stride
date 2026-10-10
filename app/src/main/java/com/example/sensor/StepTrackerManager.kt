package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.data.local.AppDatabase
import com.example.data.local.DailyStepEntity
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.max

data class SensorDiagnostics(
    val hasHardwareStepCounter: Boolean,
    val hasStepDetector: Boolean,
    val sensorName: String,
    val vendor: String,
    val powerMa: Float,
    val isBatchingSupported: Boolean,
    val isListening: Boolean,
    val isSimulationActive: Boolean
)

class StepTrackerManager private constructor(context: Context) : SensorEventListener {

    private val appContext = context.applicationContext
    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val userPreferences = UserPreferences(appContext)
    private val database = AppDatabase.getInstance(appContext)
    private val dao = database.dailyStepDao()

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _todayStepsFlow = MutableStateFlow(0)
    val todayStepsFlow: StateFlow<Int> = _todayStepsFlow.asStateFlow()

    private val _sensorDiagnosticsFlow = MutableStateFlow(buildDiagnostics(false))
    val sensorDiagnosticsFlow: StateFlow<SensorDiagnostics> = _sensorDiagnosticsFlow.asStateFlow()

    private var isRegistered = false
    private var lastDbWriteSteps = -1
    private var lastDbWriteTime = 0L

    init {
        // Load initial steps for today from DB or preferences
        scope.launch {
            val todayStr = getTodayDateString()
            val savedRecord = dao.getStepRecord(todayStr)
            val currentSteps = savedRecord?.steps ?: run {
                val sim = userPreferences.getSimulationStepsToday()
                val rebootAccrued = userPreferences.getRebootAccruedSteps()
                sim + rebootAccrued
            }
            _todayStepsFlow.value = currentSteps
            lastDbWriteSteps = currentSteps
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: StepTrackerManager? = null

        fun getInstance(context: Context): StepTrackerManager {
            return INSTANCE ?: synchronized(this) {
                val instance = StepTrackerManager(context)
                INSTANCE = instance
                instance
            }
        }

        fun getTodayDateString(): String = LocalDate.now().toString()
    }

    @Synchronized
    fun startListening() {
        if (isRegistered || sensorManager == null) return

        val targetSensor = stepCounterSensor ?: stepDetectorSensor
        if (targetSensor != null) {
            // Batching: 60,000,000 microseconds = 60 seconds batch latency to save maximum battery
            // Allowing sensor hub DSP to batch updates while AP rests
            val batchLatencyUs = 60_000_000
            val success = sensorManager.registerListener(
                this,
                targetSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                batchLatencyUs
            )
            isRegistered = success
        }
        _sensorDiagnosticsFlow.value = buildDiagnostics(isRegistered)
    }

    @Synchronized
    fun stopListening() {
        if (!isRegistered || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isRegistered = false
        _sensorDiagnosticsFlow.value = buildDiagnostics(false)

        // Persist current state immediately upon stopping
        scope.launch {
            persistCurrentStepsToDb(force = true)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val rawSensorValue = event.values[0].toLong()
            handleStepCounterEvent(rawSensorValue)
        } else if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            // Each event is 1 step detected
            handleStepDetectorEvent()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op for step counter
    }

    private fun handleStepCounterEvent(rawSensorValue: Long) {
        val todayStr = getTodayDateString()
        val trackedDate = userPreferences.getTrackedDate()

        // 1. Check for day rollover (midnight transition)
        if (trackedDate != null && trackedDate != todayStr) {
            // Finalize previous day's steps in Room DB
            scope.launch {
                persistCurrentStepsToDb(force = true)
            }
            // Reset daily tracking state for the new day
            userPreferences.setTrackedDate(todayStr)
            userPreferences.setBaseSensorValue(rawSensorValue)
            userPreferences.setLastRawSensorValue(rawSensorValue)
            userPreferences.setRebootAccruedSteps(0)
            userPreferences.setSimulationStepsToday(0)
            _todayStepsFlow.value = 0
            lastDbWriteSteps = 0
            return
        }

        if (trackedDate == null) {
            userPreferences.setTrackedDate(todayStr)
        }

        var baseValue = userPreferences.getBaseSensorValue()
        var lastRawValue = userPreferences.getLastRawSensorValue()
        var rebootAccrued = userPreferences.getRebootAccruedSteps()

        // First event initialization
        if (baseValue < 0L) {
            baseValue = rawSensorValue
            userPreferences.setBaseSensorValue(baseValue)
            lastRawValue = rawSensorValue
            userPreferences.setLastRawSensorValue(lastRawValue)
        }

        // 2. Check for device reboot
        // If rawSensorValue is smaller than last recorded raw value, device rebooted!
        if (rawSensorValue < lastRawValue) {
            val stepsBeforeReboot = max(0L, lastRawValue - baseValue).toInt()
            rebootAccrued += stepsBeforeReboot
            userPreferences.setRebootAccruedSteps(rebootAccrued)
            baseValue = rawSensorValue
            userPreferences.setBaseSensorValue(baseValue)
        }

        // Update last raw value
        userPreferences.setLastRawSensorValue(rawSensorValue)

        // Calculate today's steps
        val rawDelta = max(0L, rawSensorValue - baseValue).toInt()
        val simSteps = userPreferences.getSimulationStepsToday()
        val totalTodaySteps = rawDelta + rebootAccrued + simSteps

        _todayStepsFlow.value = totalTodaySteps

        // Debounced DB write
        debouncedPersist(totalTodaySteps)
    }

    private fun handleStepDetectorEvent() {
        val todayStr = getTodayDateString()
        val trackedDate = userPreferences.getTrackedDate()
        if (trackedDate != todayStr) {
            scope.launch {
                persistCurrentStepsToDb(force = true)
            }
            userPreferences.setTrackedDate(todayStr)
            _todayStepsFlow.value = 0
            lastDbWriteSteps = 0
        }

        val newSteps = _todayStepsFlow.value + 1
        _todayStepsFlow.value = newSteps
        debouncedPersist(newSteps)
    }

    /**
     * Simulation / manual testing helper.
     * Allows seamless testing in emulator or non-sensor environments.
     */
    fun addManualSteps(count: Int) {
        val todayStr = getTodayDateString()
        val trackedDate = userPreferences.getTrackedDate()
        if (trackedDate != todayStr) {
            scope.launch {
                persistCurrentStepsToDb(force = true)
            }
            userPreferences.setTrackedDate(todayStr)
            _todayStepsFlow.value = 0
            lastDbWriteSteps = 0
        }

        val currentSim = userPreferences.getSimulationStepsToday()
        val newSim = currentSim + count
        userPreferences.setSimulationStepsToday(newSim)

        val newTotal = _todayStepsFlow.value + count
        _todayStepsFlow.value = newTotal

        scope.launch {
            persistCurrentStepsToDb(force = true)
        }
    }

    fun resetTodaySteps() {
        val todayStr = getTodayDateString()
        userPreferences.setTrackedDate(todayStr)
        val lastRaw = userPreferences.getLastRawSensorValue()
        if (lastRaw >= 0L) {
            userPreferences.setBaseSensorValue(lastRaw)
        }
        userPreferences.setRebootAccruedSteps(0)
        userPreferences.setSimulationStepsToday(0)
        _todayStepsFlow.value = 0
        lastDbWriteSteps = 0

        scope.launch {
            val goal = userPreferences.getStepGoal()
            dao.insertOrUpdate(
                DailyStepEntity(
                    date = todayStr,
                    steps = 0,
                    goal = goal,
                    distanceKm = 0f,
                    calories = 0
                )
            )
        }
    }

    private fun debouncedPersist(steps: Int) {
        val now = System.currentTimeMillis()
        // Save if step delta >= 10 or 30 seconds elapsed
        if (steps - lastDbWriteSteps >= 10 || (now - lastDbWriteTime > 30_000L && steps != lastDbWriteSteps)) {
            scope.launch {
                persistCurrentStepsToDb(force = false)
            }
        }
    }

    suspend fun persistCurrentStepsToDb(force: Boolean = false) {
        val todayStr = getTodayDateString()
        val steps = _todayStepsFlow.value
        val goal = userPreferences.getStepGoal()
        val strideLengthCm = userPreferences.getStrideLengthCm()

        // Distance in km: steps * (strideLengthCm / 100) / 1000 = steps * strideLengthCm / 100,000
        val distanceKm = (steps * strideLengthCm) / 100000f
        // Standard moderate walking expenditure ~0.04 kcal per step
        val calories = (steps * 0.04f).toInt()

        val record = DailyStepEntity(
            date = todayStr,
            steps = steps,
            goal = goal,
            distanceKm = distanceKm,
            calories = calories,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertOrUpdate(record)
        lastDbWriteSteps = steps
        lastDbWriteTime = System.currentTimeMillis()
    }

    private fun buildDiagnostics(listening: Boolean): SensorDiagnostics {
        val primary = stepCounterSensor ?: stepDetectorSensor
        return SensorDiagnostics(
            hasHardwareStepCounter = stepCounterSensor != null,
            hasStepDetector = stepDetectorSensor != null,
            sensorName = primary?.name ?: "Hardware Step Counter (Virtual / Emulated)",
            vendor = primary?.vendor ?: "System Platform",
            powerMa = primary?.power ?: 0.03f, // Typical hardware step counter DSP is ~0.02 - 0.05 mA
            isBatchingSupported = (primary?.fifoMaxEventCount ?: 0) > 0,
            isListening = listening,
            isSimulationActive = stepCounterSensor == null
        )
    }
}
