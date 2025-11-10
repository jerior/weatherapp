package ru.burchik.myweatherapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.burchik.myweatherapp.domain.util.NetworkResult
import ru.burchik.myweatherapp.BuildConfig
import ru.burchik.myweatherapp.data.mapper.toWeather
import ru.burchik.myweatherapp.data.source.remote.api.WeatherApi
import ru.burchik.myweatherapp.data.source.remote.base.safeApiFlow
import ru.burchik.myweatherapp.domain.model.Weather
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi
) : WeatherRepository {

    override fun getWeather(location: String): Flow<NetworkResult<Weather>> {
        return safeApiFlow {
            val response = api.getWeather(
                apiKey = BuildConfig.weatherapi_apikey,
                location = location
            )
            response.toWeather()
        }
    }
}