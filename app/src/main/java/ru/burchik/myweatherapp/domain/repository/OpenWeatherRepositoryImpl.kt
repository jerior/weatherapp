package ru.burchik.myweatherapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.burchik.myweatherapp.domain.util.NetworkResult
import ru.burchik.myweatherapp.BuildConfig
import ru.burchik.myweatherapp.data.source.remote.api.OpenWeatherApi
import ru.burchik.myweatherapp.data.source.remote.mapper.toWeather
import ru.burchik.myweatherapp.data.source.remote.base.safeApiFlow
import ru.burchik.myweatherapp.domain.model.Weather
import javax.inject.Inject

class OpenWeatherRepositoryImpl @Inject constructor(
    private val api: OpenWeatherApi
) : WeatherRepository {

    override fun getWeather(query: String): Flow<NetworkResult<Weather>> {
        return safeApiFlow {
            val response = api.getWeatherByCity(
                openWeatherApiKey = BuildConfig.openweather_apikey,
                query = query,
                days = 3
            )
            response.toWeather()
        }
    }
}