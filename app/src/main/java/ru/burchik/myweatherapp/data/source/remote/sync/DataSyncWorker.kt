package ru.burchik.myweatherapp.data.source.remote.sync

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ru.burchik.myweatherapp.glance.WeatherWidget

class DataSyncWorker(
    val context: Context,
    val params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Fetch data or do some work and then update all instance of your widget

        WeatherWidget().updateAll(context)
        return Result.success()
    }
}