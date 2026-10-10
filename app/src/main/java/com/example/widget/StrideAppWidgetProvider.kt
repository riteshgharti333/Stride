package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.preferences.UserPreferences
import com.example.sensor.StepTrackerManager

class StrideAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = UserPreferences(context)
        val tracker = StepTrackerManager.getInstance(context)
        val steps = tracker.todayStepsFlow.value
        val goal = prefs.getStepGoal()

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, steps, goal)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context, steps: Int, goal: Int) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, StrideAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, steps, goal)
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            steps: Int,
            goal: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_stride)

            val formattedSteps = String.format("%,d", steps)
            val formattedGoal = String.format("%,d", goal)
            val percent = if (goal > 0) ((steps.toFloat() / goal) * 100).toInt() else 0

            views.setTextViewText(R.id.widget_step_count, formattedSteps)
            views.setTextViewText(R.id.widget_goal_subtext, "$percent% of $formattedGoal goal")
            views.setProgressBar(R.id.widget_progress_bar, 100, percent.coerceIn(0, 100), false)

            // Click to open main app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
