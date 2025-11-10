package ru.burchik.myweatherapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.burchik.myweatherapp.domain.util.NetworkResult
import ru.burchik.myweatherapp.domain.model.Weather

interface WeatherRepository {
    fun getWeather(location: String): Flow<NetworkResult<Weather>>
}