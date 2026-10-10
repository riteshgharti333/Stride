package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.preferences.UserPreferences
import com.example.service.StepTrackingService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val prefs = UserPreferences(context)
            if (prefs.isBackgroundServiceEnabled() && StepTrackingService.hasRequiredPermissions(context)) {
                StepTrackingService.start(context)
            }
        }
    }
}
