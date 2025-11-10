package ru.burchik.myweatherapp.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class WeatherResponse(
    val location: LocationDto,
    val current: CurrentWeatherDto,
    val forecast: ForecastDto
)

data class LocationDto(
    val name: String,
    val country: String
)

data class CurrentWeatherDto(
    @SerializedName("temp_c")
    val tempC: Double,
    @SerializedName("feelslike_c")
    val feelsLikeC: Double,
    val humidity: Int,
    @SerializedName("wind_kph")
    val windKph: Double,
    val condition: ConditionDto
)

data class ForecastDto(
    @SerializedName("forecastday")
    val forecastDay: List<ForecastDayDto>
)

data class ForecastDayDto(
    val date: String,
    @SerializedName("date_epoch")
    val dateEpoch: Long,
    val day: DayDto,
    val hour: List<HourDto>
)

data class HourDto(
    val time: String,
    @SerializedName("temp_c")
    val tempC: Double,
    @SerializedName("wind_kph")
    val windKph: Double,
    @SerializedName("chance_of_rain")
    val chanceOfRain: Int,
    val condition: ConditionDto
)

data class DayDto(
    @SerializedName("maxtemp_c")
    val maxTempC: Double,
    @SerializedName("mintemp_c")
    val minTempC: Double,
    @SerializedName("daily_chance_of_rain")
    val chanceOfRain: Int,
    val condition: ConditionDto
)

data class ConditionDto(
    val text: String,
    val icon: String
)