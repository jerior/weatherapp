package ru.burchik.myweatherapp.ui.weatherOverview

sealed class WeatherEvent {
    data class GetWeatherByQuery(val location: String) : WeatherEvent()
    data class GetWeatherByLocation(val location: String) : WeatherEvent()
    data class UpdateSearchQuery(val query: String) : WeatherEvent()
    data object RetryLastSearch : WeatherEvent()
    data object ToggleLocationBasedSearch : WeatherEvent()
    data object ToggleSearchBarVisibility : WeatherEvent()
}