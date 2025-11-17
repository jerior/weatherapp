package ru.burchik.myweatherapp.glance

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit

class WeatherWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = WeatherWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Timber.d("Widget enabled - first instance added")

        // Schedule periodic updates
        schedulePeriodicUpdates(context)

        // Trigger immediate update
        triggerImmediateUpdate(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Timber.d( "Widget updated - ${appWidgetIds.size} instance(s)")

        // Trigger update when widget is added
        triggerImmediateUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Timber.d("Widget disabled - last instance removed")

        // Cancel all work when no widgets remain
        cancelPeriodicUpdates(context)
    }

    private fun schedulePeriodicUpdates(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
            repeatInterval = 30, // Update every 30 minutes
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 5, // Allow 5 minute flex
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing schedule
            workRequest
        )

        Timber.d("Periodic updates scheduled (every 30 minutes)")
    }

    private fun triggerImmediateUpdate(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<WeatherUpdateWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Timber.d("Immediate update triggered")
    }

    private fun cancelPeriodicUpdates(context: Context) {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(PERIODIC_WORK_NAME)
            cancelUniqueWork(IMMEDIATE_WORK_NAME)
        }
        Timber.d("All scheduled work cancelled")
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "weather_widget_periodic_update"
        private const val IMMEDIATE_WORK_NAME = "weather_widget_immediate_update"
    }
}