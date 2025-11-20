package ru.burchik.myweatherapp.ui.weatherOverview

sealed class WeatherEvent {
    data object LoadInitialWeather : WeatherEvent()
    data class GetWeatherByQuery(val location: String) : WeatherEvent()
    data object GetWeatherByLocation : WeatherEvent()
    data class UpdateSearchQuery(val query: String) : WeatherEvent()
    data object RetryLastSearch : WeatherEvent()
    data object ToggleLocationBasedSearch : WeatherEvent()
    data object ToggleSearchBarVisibility : WeatherEvent()
}