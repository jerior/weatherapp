package ru.burchik.myweatherapp.ui.weatherOverview

sealed class WeatherEvent {
    data class SearchWeather(val location: String) : WeatherEvent()
    data class UpdateSearchQuery(val query: String) : WeatherEvent()
    data object RetryLastSearch : WeatherEvent()
}