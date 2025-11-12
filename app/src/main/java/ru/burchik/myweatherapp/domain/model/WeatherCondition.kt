package ru.burchik.myweatherapp.domain.model

import ru.burchik.myweatherapp.R

typealias DrawableResId = Int
typealias StringResId = Int

sealed interface WeatherCondition {
    fun getDrawableResId(): DrawableResId
    fun getStringResId(): StringResId

    data object ClearSky : WeatherCondition {
        override fun getDrawableResId(): DrawableResId = R.drawable.sun_forecast_sunset_weather
        override fun getStringResId(): StringResId =  R.string.clear_sky
    }
    data object MostlyClear : WeatherCondition {
        override fun getDrawableResId(): DrawableResId = R.drawable.forecast_cloudy_cloud_weather_sun
        override fun getStringResId(): StringResId =  R.string.mostly_clear
    }
    data object Cloudy: WeatherCondition {
        override fun getDrawableResId(): DrawableResId = R.drawable.forecast_weather_cloudy_cloud
        override fun getStringResId(): StringResId =  R.string.cloudy
    }
    data object LightRain : WeatherCondition {
        override fun getDrawableResId(): DrawableResId = R.drawable.day_shine_sun_rain_forecast_weather_cloud
        override fun getStringResId(): StringResId =  R.string.light_rain
    }
    data object HeavyRain : WeatherCondition {
        override fun getDrawableResId(): DrawableResId = R.drawable.rain_drop_weather_cloud_forecast
        override fun getStringResId(): StringResId =  R.string.heavy_rain
    }
    data object Thunderstorm : WeatherCondition {
        override fun getDrawableResId(): DrawableResId = R.drawable.thunder_lightening_weather_cloud_storm
        override fun getStringResId(): StringResId =  R.string.thunderstorm
    }
    data object Snow : WeatherCondition {
        override fun getDrawableResId(): DrawableResId = R.drawable.weather_winter_forecast_snow_christmas_snowflake_cold
        override fun getStringResId(): StringResId =  R.string.snow
    }
    data object Fog : WeatherCondition {
        override fun getDrawableResId(): DrawableResId = R.drawable.sunrise_morning_sun //todo contraversion icon
        override fun getStringResId(): StringResId =  R.string.fog
    }
}