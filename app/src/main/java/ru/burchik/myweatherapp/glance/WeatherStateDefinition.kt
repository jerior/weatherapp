package ru.burchik.myweatherapp.glance

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.state.GlanceStateDefinition
import java.io.File

object WeatherStateDefinition : GlanceStateDefinition<Preferences> {
    private const val PREFS_NAME = "weather_widget_prefs"

    private val Context.weatherDataStore by preferencesDataStore(PREFS_NAME)

    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<Preferences> {
        return context.weatherDataStore
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(PREFS_NAME)
    }
}

object WeatherPrefsKeys {
    val WEATHER_JSON = stringPreferencesKey("weather_json")
    val LAST_UPDATE = longPreferencesKey("last_update")
    val LOCATION = stringPreferencesKey("location")
    val IS_LOCATION_BASED = booleanPreferencesKey("is_location_based")
    val ERROR_MESSAGE = stringPreferencesKey("error_message")
}