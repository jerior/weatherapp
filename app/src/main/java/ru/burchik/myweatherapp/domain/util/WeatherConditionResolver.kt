package ru.burchik.myweatherapp.domain.util

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ru.burchik.myweatherapp.R
import ru.burchik.myweatherapp.domain.model.WeatherCondition

enum class IconSet {
    DEFAULT,
    BICOLOR
}

class WeatherConditionResolver(
    private val context: Context,
    private val iconSet: IconSet = IconSet.DEFAULT
) {

    /**
     * Resolves the drawable resource ID for a given weather condition
     */
    @DrawableRes
    fun resolveIcon(condition: WeatherCondition): Int {
        return when (iconSet) {
            IconSet.DEFAULT -> resolveDefaultIcon(condition)
            IconSet.BICOLOR -> resolveColorfulIcon(condition)
        }
    }

    /**
     * Resolves the localized string resource ID for a given weather condition
     */
    @StringRes
    fun resolveLocalizedName(condition: WeatherCondition): Int {
        return when (condition) {
            WeatherCondition.CLEAR -> R.string.weather_clear
            WeatherCondition.PARTLY_CLOUDY -> R.string.weather_partly_cloudy
            WeatherCondition.CLOUDY -> R.string.weather_cloudy
            WeatherCondition.OVERCAST -> R.string.weather_overcast
            WeatherCondition.RAIN -> R.string.weather_rain
            WeatherCondition.DRIZZLE -> R.string.weather_drizzle
            WeatherCondition.HEAVY_RAIN -> R.string.weather_heavy_rain
            WeatherCondition.THUNDERSTORM -> R.string.weather_thunderstorm
            WeatherCondition.SNOW -> R.string.weather_snow
            WeatherCondition.SLEET -> R.string.weather_sleet
            WeatherCondition.FOG -> R.string.weather_fog
            WeatherCondition.WIND -> R.string.weather_wind
            WeatherCondition.UNKNOWN -> R.string.weather_unknown
        }
    }

    /**
     * Gets the localized string directly
     */
    fun getLocalizedName(condition: WeatherCondition): String {
        return context.getString(resolveLocalizedName(condition))
    }

    private fun resolveDefaultIcon(condition: WeatherCondition): Int {
        return when (condition) {
            WeatherCondition.CLEAR -> R.drawable.sun_weather_forecast
            WeatherCondition.PARTLY_CLOUDY -> R.drawable.forecast_cloudy_cloud_weather_sun
            WeatherCondition.CLOUDY -> R.drawable.forecast_cloudy_cloud_weather_sun
            WeatherCondition.OVERCAST -> R.drawable.forecast_cloudy_cloud_weather_sun
            WeatherCondition.RAIN -> R.drawable.day_shine_sun_rain_forecast_weather_cloud
            WeatherCondition.DRIZZLE -> R.drawable.day_shine_sun_rain_forecast_weather_cloud
            WeatherCondition.HEAVY_RAIN -> R.drawable.rain_drop_weather_cloud_forecast
            WeatherCondition.THUNDERSTORM -> R.drawable.thunder_lightening_weather_cloud_storm
            WeatherCondition.SNOW -> R.drawable.weather_winter_forecast_snow_christmas_snowflake_cold
            WeatherCondition.SLEET -> R.drawable.weather_winter_forecast_snow_christmas_snowflake_cold
            WeatherCondition.FOG -> R.drawable.sun_forecast_sunset_weather
            WeatherCondition.WIND -> R.drawable.colorful_rainbow_magic_magician_fantasy_fairytale
            WeatherCondition.UNKNOWN -> R.drawable.colorful_rainbow_magic_magician_fantasy_fairytale
        }
    }

    private fun resolveColorfulIcon(condition: WeatherCondition): Int {
        return when (condition) {
            WeatherCondition.CLEAR -> R.drawable.__weather_sun_sunny_temperature
            WeatherCondition.PARTLY_CLOUDY -> R.drawable.__clouds_sun_sunny_weather
            WeatherCondition.CLOUDY -> R.drawable.__cloudy_weather_clouds_cloud
            WeatherCondition.OVERCAST -> R.drawable.__clouds_sun_sunny_weather
            WeatherCondition.RAIN -> R.drawable.__rain_cloud_drizzel_weather
            WeatherCondition.DRIZZLE -> R.drawable.__rain_cloud_drizzel_weather
            WeatherCondition.HEAVY_RAIN -> R.drawable.__rain_storm_shower_weather
            WeatherCondition.THUNDERSTORM -> R.drawable.__storm_weather_thunder_clouds_rain
            WeatherCondition.SNOW -> R.drawable.__winter_snowflake_christmas_xmas_weather_snow
            WeatherCondition.SLEET -> R.drawable.__hail_weather_hailstone_clouds_snow
            WeatherCondition.FOG -> R.drawable.__foggy_weather_fog_clouds_cloudy
            WeatherCondition.WIND -> R.drawable.__windy_cloudy_storm_wind_weather
            WeatherCondition.UNKNOWN -> R.drawable.__cloudy_weather_clouds_cloud
        }
    }
}