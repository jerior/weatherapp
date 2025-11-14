package ru.burchik.myweatherapp.glance

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import ru.burchik.myweatherapp.glance.WeatherWidget.WeatherProviderEntryPoint

class WeatherRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker( appContext, workerParams ){

    companion object {
        const val KEY_LOCATION = "location"
    }

    override suspend fun doWork(): Result {
        val queryLocation = inputData.getString(KEY_LOCATION) ?: return Result.failure()

        val statisticsEntryPoint =
            EntryPointAccessors.fromApplication(
                applicationContext,
                WeatherProviderEntryPoint::class.java,
            )
        val weatherRepository = statisticsEntryPoint.weatherRepository()

        try {
            // Simulate fetching data
            val newData = "Data fetched at ${System.currentTimeMillis()}"

            // Store data in DataStore
            //weatherRepository.getWeather("Yekaterinburg").collect {  }

            // Update the Glance widget
            WeatherWidget().updateAll(applicationContext)

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}