package ru.burchik.myweatherapp.glance

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import ru.burchik.myweatherapp.glance.WeatherWidget.WeatherProviderEntryPoint
import timber.log.Timber

class WeatherRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker( appContext, workerParams ){

    companion object {
        const val KEY_LOCATION = "location"
    }

    override suspend fun doWork(): Result {
        val queryLocation = inputData.getString(KEY_LOCATION) ?: return Result.failure()
        Timber.d("Query location: $queryLocation")

        val statisticsEntryPoint =
            EntryPointAccessors.fromApplication(
                applicationContext,
                WeatherProviderEntryPoint::class.java,
            )
        val weatherRepository = statisticsEntryPoint.weatherRepository()

        try {
            // Fetching data
            weatherRepository.getWeather("Yekaterinburg").collect {

            }

            // Store data in DataStore


            // Update the Glance widget
            WeatherWidget().updateAll(applicationContext)

            return Result.success()
        } catch (e: Exception) {
            Timber.e(e)
            return Result.failure()
        }
    }
}