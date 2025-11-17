package ru.burchik.myweatherapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Weather(
    val location: String,
    val country: String,
    val temperature: Double,
    val condition: WeatherCondition,
    val humidity: Int,
    val windSpeed: Double,
    val feelsLike: Double,
    val hourlyForecast: List<HourlyForecast>,
    val forecast: List<ForecastDay>
)

@Serializable
data class HourlyForecast(
    val time: String,
    val temperature: Double,
    val condition: WeatherCondition,
    val chanceOfRain: Int,
    val windSpeed: Double
)

@Serializable
data class ForecastDay(
    val date: String,
    val dateEpoch: Long,
    val maxTemp: Double,
    val minTemp: Double,
    val condition: WeatherCondition,
    val chanceOfRain: Int
)