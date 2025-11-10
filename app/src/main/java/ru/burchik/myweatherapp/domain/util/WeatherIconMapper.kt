package ru.burchik.myweatherapp.domain.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.ui.graphics.vector.ImageVector

object WeatherIconMapper {

    fun getIconByCondition(condition: String, isDay: Boolean = true): ImageVector {
        return when {
            condition.contains("sunny", ignoreCase = true) ||
                    condition.contains("clear", ignoreCase = true) ->
                if (isDay) Icons.Filled.WbSunny else Icons.Outlined.Nightlight

            condition.contains("partly cloudy", ignoreCase = true) ||
                    condition.contains("partly sunny", ignoreCase = true) ->
                if (isDay) Icons.Outlined.WbCloudy else Icons.Outlined.NightsStay

            condition.contains("cloudy", ignoreCase = true) ||
                    condition.contains("overcast", ignoreCase = true) ->
                Icons.Filled.Cloud

            condition.contains("mist", ignoreCase = true) ||
                    condition.contains("fog", ignoreCase = true) ->
                Icons.Outlined.CloudQueue

            condition.contains("rain", ignoreCase = true) ||
                    condition.contains("drizzle", ignoreCase = true) ||
                    condition.contains("shower", ignoreCase = true) ->
                Icons.Outlined.Umbrella

            condition.contains("snow", ignoreCase = true) ||
                    condition.contains("sleet", ignoreCase = true) ||
                    condition.contains("ice", ignoreCase = true) ||
                    condition.contains("freezing", ignoreCase = true) ||
                    condition.contains("blizzard", ignoreCase = true) ->
                Icons.Outlined.AcUnit

            condition.contains("thunder", ignoreCase = true) ||
                    condition.contains("storm", ignoreCase = true) ->
                Icons.Outlined.Thunderstorm

            condition.contains("wind", ignoreCase = true) ->
                Icons.Outlined.Air

            else -> if (isDay) Icons.Filled.WbSunny else Icons.Outlined.Nightlight
        }
    }
}