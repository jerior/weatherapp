package ru.burchik.myweatherapp.domain.model

enum class WeatherCondition {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    OVERCAST,
    RAIN,
    DRIZZLE,
    HEAVY_RAIN,
    THUNDERSTORM,
    SNOW,
    SLEET,
    FOG,
    WIND,
    UNKNOWN;

    companion object {
        fun fromString(value: String): WeatherCondition {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
}