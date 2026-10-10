package com.example.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.preferences.UserPreferences
import com.example.sensor.StepTrackerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StepTrackingService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private lateinit var stepTrackerManager: StepTrackerManager
    private lateinit var userPreferences: UserPreferences
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val CHANNEL_ID = "stride_step_tracking"
        const val NOTIFICATION_ID = 1001

        fun hasRequiredPermissions(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val hasActivityRecognition = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasActivityRecognition) return false
            }
            return true
        }

        fun start(context: Context) {
            if (!hasRequiredPermissions(context)) {
                return
            }
            try {
                val intent = Intent(context, StepTrackingService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {
                // Prevent crashes from background start restrictions
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, StepTrackingService::class.java)
                context.stopService(intent)
            } catch (_: Exception) {
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (!hasRequiredPermissions(this)) {
            stopSelf()
            return
        }

        stepTrackerManager = StepTrackerManager.getInstance(this)
        userPreferences = UserPreferences(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()

        val started = startForegroundSafely(stepTrackerManager.todayStepsFlow.value)
        if (!started) {
            stopSelf()
            return
        }

        stepTrackerManager.startListening()

        serviceScope.launch {
            stepTrackerManager.todayStepsFlow.collectLatest { steps ->
                updateNotification(steps)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!hasRequiredPermissions(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (::stepTrackerManager.isInitialized) {
            stepTrackerManager.startListening()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        if (::stepTrackerManager.isInitialized) {
            stepTrackerManager.stopListening()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Stride Daily Step Counter",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live daily step count silently with minimal battery usage."
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(steps: Int): Notification {
        val goal = if (::userPreferences.isInitialized) userPreferences.getStepGoal() else 8000
        val percent = if (goal > 0) ((steps.toFloat() / goal) * 100).toInt() else 0

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedSteps = String.format("%,d", steps)
        val formattedGoal = String.format("%,d", goal)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("$formattedSteps steps today")
            .setContentText("$percent% of daily goal ($formattedGoal)")
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun startForegroundSafely(steps: Int): Boolean {
        return try {
            val notification = buildNotification(steps)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                } else {
                    0
                }
                if (serviceType != 0) {
                    startForeground(NOTIFICATION_ID, notification, serviceType)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            true
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun updateNotification(steps: Int) {
        try {
            val notification = buildNotification(steps)
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {
        }
    }
}
