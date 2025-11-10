package ru.burchik.myweatherapp.domain.model

data class Weather(
    val location: String,
    val country: String,
    val temperature: Double,
    val condition: String,
    val conditionIcon: String,
    val humidity: Int,
    val windSpeed: Double,
    val feelsLike: Double,
    val hourlyForecast: List<HourlyForecast>,
    val forecast: List<ForecastDay>
)

data class HourlyForecast(
    val time: String,
    val temperature: Double,
    val condition: String,
    val conditionIcon: String,
    val chanceOfRain: Int,
    val windSpeed: Double
)

data class ForecastDay(
    val date: String,
    val dateEpoch: Long,
    val maxTemp: Double,
    val minTemp: Double,
    val condition: String,
    val conditionIcon: String,
    val chanceOfRain: Int
)