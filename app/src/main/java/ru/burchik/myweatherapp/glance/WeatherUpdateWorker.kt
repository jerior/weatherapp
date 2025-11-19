package ru.burchik.myweatherapp.glance

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.burchik.myweatherapp.BuildConfig
import ru.burchik.myweatherapp.data.source.remote.api.WeatherApi
import ru.burchik.myweatherapp.data.source.remote.mapper.toWeather
import ru.burchik.myweatherapp.domain.model.Weather
import ru.burchik.myweatherapp.utils.LocationProvider
import ru.burchik.myweatherapp.utils.WeatherSerializer
import timber.log.Timber

class WeatherUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WeatherApiProviderEntryPoint {
        fun weatherApi(): WeatherApi
    }

    @SuppressLint("MissingPermission", "RestrictedApi")
    override suspend fun doWork(): Result {
        Timber.d("Starting weather update")

        val loc = LocationProvider(applicationContext).getCurrentLocation()?.let{ "${it.latitude},${it.longitude}" }  ?: "Moscow"
        CoroutineScope(Dispatchers.IO).launch {
            val weather = fetchWeatherDataDirectlyFromApi(location = loc)
            Timber.d("Weather: $weather")
            updateWeatherState(weather)
        }

        // If we reach here, no success result was emitted
        updateErrorState("Failed to fetch weather data")
        return Result.Failure()
    }


    private suspend fun updateErrorState(message: String) {
        withContext(Dispatchers.IO) {
            val glanceIds = GlanceAppWidgetManager(applicationContext)
                .getGlanceIds(WeatherWidget::class.java)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(applicationContext, glanceId) { prefs ->
                    prefs[WeatherPrefsKeys.ERROR_MESSAGE] = message
                }
                WeatherWidget().update(applicationContext, glanceId)
            }
        }
    }

    private suspend fun fetchWeatherDataDirectlyFromApi(location: String): Weather =
        withContext(Dispatchers.IO) {
            val apiEntryPoint =
                EntryPointAccessors.fromApplication(
                    applicationContext,
                    WeatherApiProviderEntryPoint::class.java,
                )
            val weatherApi = apiEntryPoint.weatherApi()

            weatherApi.getWeather(BuildConfig.weatherapi_apikey, location, 3)
                .toWeather()

        }

    private suspend fun updateWeatherState(weather: Weather) {
        withContext(Dispatchers.IO) {
            // Serialize the entire Weather object to JSON
            val weatherJson = WeatherSerializer.toJson(weather)

            val glanceIds = GlanceAppWidgetManager(applicationContext)
                .getGlanceIds(WeatherWidget::class.java)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(applicationContext, glanceId) { prefs ->
                    prefs[WeatherPrefsKeys.WEATHER_JSON] = weatherJson
                    prefs[WeatherPrefsKeys.LAST_UPDATE] = System.currentTimeMillis()
                    prefs[WeatherPrefsKeys.LOCATION] = weather.location
                    prefs[WeatherPrefsKeys.ERROR_MESSAGE] = "" // Clear error
                }
                WeatherWidget().update(applicationContext, glanceId)
            }
            Timber.d("Weather state updated in DataStore")
        }
    }
}