package ru.burchik.myweatherapp.utils

import kotlinx.serialization.json.Json
import ru.burchik.myweatherapp.domain.model.Weather
import timber.log.Timber

object WeatherSerializer {
    private val json = Json {
        ignoreUnknownKeys = true  // Ignore fields not in your model
        encodeDefaults = true      // Include default values
        isLenient = true           // Allow non-strict JSON
    }

    fun toJson(weather: Weather): String {
        return try {
            json.encodeToString(weather)
        } catch (e: Exception) {
            Timber.e( e)
            throw e
        }
    }

    fun fromJson(jsonString: String): Weather? {
        return try {
            json.decodeFromString<Weather>(jsonString)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }
}