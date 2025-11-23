package ru.burchik.myweatherapp.glance

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import ru.burchik.myweatherapp.BuildConfig
import ru.burchik.myweatherapp.data.preferences.UserPreferencesRepository
import ru.burchik.myweatherapp.data.source.remote.api.WeatherApi
import ru.burchik.myweatherapp.data.source.remote.mapper.toWeather
import ru.burchik.myweatherapp.domain.model.Weather
import ru.burchik.myweatherapp.domain.repository.WeatherRepository
import ru.burchik.myweatherapp.domain.util.NetworkResult
import ru.burchik.myweatherapp.utils.LocationProvider
import ru.burchik.myweatherapp.utils.WeatherSerializer
import timber.log.Timber

class WeatherUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WeatherRepoProviderEntryPoint {
        fun weatherRepository(): WeatherRepository
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppPreferencesRepoProviderEntryPoint {
        fun userPrefsRepo(): UserPreferencesRepository
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WeatherApiProviderEntryPoint {
        fun weatherApi(): WeatherApi
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission", "RestrictedApi")
    override suspend fun doWork(): Result {
        Timber.d("Starting weather update")

        val prefRepoEntryPoint =
            EntryPointAccessors.fromApplication(
                applicationContext,
                AppPreferencesRepoProviderEntryPoint::class.java,
            )
        val userPrefsRepo = prefRepoEntryPoint.userPrefsRepo()

        val weatherRepoEntryPoint =
            EntryPointAccessors.fromApplication(
                applicationContext,
                WeatherRepoProviderEntryPoint::class.java,
            )
        val weatherRepo = weatherRepoEntryPoint.weatherRepository()

        userPrefsRepo.userPreferences
            .onEach { Timber.d("1. doWork User preferences: $it") }
            .flatMapLatest { prefs ->
                if (prefs.isLocationBased) {
                    flowOf(LocationProvider(applicationContext).getCurrentLocation()?.let{ "${it.latitude},${it.longitude}" } ?: "Moscow")
                } else {
                    flowOf(prefs.lastSearchedLocation)
                }
            }
            .onEach { Timber.d("2. doWork Location: $it") }
            .flatMapLatest {location ->
                weatherRepo.getWeather (query = location)
            }.collectLatest {
                when (it) {
                    is NetworkResult.Success -> {
                        Timber.d("3. doWork Success: $it")
                        updateWeatherState(it.data)
                        Result.success()
                    }

                    is NetworkResult.Error -> {
                        updateErrorState(it.error.toString())
                    }
                    else -> {}
                }
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

    //todo need to be reused and centralized
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

/*    suspend fun updateAppWeatherStatePrefs() {
        applicationContext.dataStore.updateData {
            it.toMutablePreferences().also { preferences ->
                preferences["weather"] = (preferences[EXAMPLE_COUNTER] ?: 0) + 1
            }
        }
    }*/

    private suspend fun updateWeatherState(weather: Weather) {
        withContext(Dispatchers.IO) {
            // Serialize the entire Weather object to JSON
            val weatherJson = WeatherSerializer.toJson(weather)

            //update app preferences
            applicationContext.dataStore.updateData {
                it.toMutablePreferences().also { preferences ->
                    preferences[WeatherPrefsKeys.WEATHER_JSON] = weatherJson
                    preferences[WeatherPrefsKeys.LAST_UPDATE] = System.currentTimeMillis()
                    preferences[WeatherPrefsKeys.LAST_LOCATION] = weather.location
                }
            }

            val glanceIds = GlanceAppWidgetManager(applicationContext)
                .getGlanceIds(WeatherWidget::class.java)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(applicationContext, glanceId) { prefs ->
                    prefs[WeatherPrefsKeys.WEATHER_JSON] = weatherJson
                    prefs[WeatherPrefsKeys.LAST_UPDATE] = System.currentTimeMillis()
                    prefs[WeatherPrefsKeys.LAST_LOCATION] = weather.location
                    prefs[WeatherPrefsKeys.ERROR_MESSAGE] = "" // Clear error
                }
                WeatherWidget().update(applicationContext, glanceId)
            }
            Timber.d("Weather state updated in DataStore")
        }
    }
}