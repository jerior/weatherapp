package ru.burchik.myweatherapp.data.mapper

import ru.burchik.myweatherapp.data.source.remote.dto.WeatherResponse
import ru.burchik.myweatherapp.domain.model.ForecastDay
import ru.burchik.myweatherapp.domain.model.HourlyForecast
import ru.burchik.myweatherapp.domain.model.Weather
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun WeatherResponse.toWeather(): Weather {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    // Get hourly forecast for today only (next 24 hours from current time)
    val todayHourly = forecast.forecastDay.firstOrNull()?.hour
        ?.filter { hour ->
            // Parse hour from time string "2024-01-15 14:00"
            val hourOfDay = hour.time.split(" ")[1].split(":")[0].toInt()
            hourOfDay >= currentHour
        }
        ?.take(12) // Show next 12 hours
        ?.map { hour ->
            HourlyForecast(
                time = formatHourTime(hour.time),
                temperature = hour.tempC,
                condition = hour.condition.text,
                conditionIcon = "https:${hour.condition.icon}",
                chanceOfRain = hour.chanceOfRain,
                windSpeed = hour.windKph
            )
        } ?: emptyList()

    return Weather(
        location = location.name,
        country = location.country,
        temperature = current.tempC,
        condition = current.condition.text,
        conditionIcon = "https:${current.condition.icon}",
        humidity = current.humidity,
        windSpeed = current.windKph,
        feelsLike = current.feelsLikeC,
        hourlyForecast = todayHourly,
        forecast = forecast.forecastDay.map { day ->
            ForecastDay(
                date = day.date,
                maxTemp = day.day.maxTempC,
                minTemp = day.day.minTempC,
                condition = day.day.condition.text,
                conditionIcon = "https:${day.day.condition.icon}",
                chanceOfRain = day.day.chanceOfRain,
                dateEpoch = day.dateEpoch
            )
        }
    )
}

private fun formatHourTime(timeString: String): String {
    // Input: "2024-01-15 14:00"
    // Output: "14:00"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(timeString)
        date?.let { outputFormat.format(it) } ?: timeString.split(" ")[1]
    } catch (e: Exception) {
        //questionable :-)
        timeString.split(" ").getOrNull(1) ?: timeString
    }
}