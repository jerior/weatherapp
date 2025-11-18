package ru.burchik.myweatherapp.ui.weatherOverview

import ru.burchik.myweatherapp.domain.model.Weather

data class WeatherState(
    val isLoading: Boolean = false,
    val weather: Weather? = null,
    val error: String = "",
    val searchQuery: String = "Москва",
    val lastSearchedLocation: String = "Москва", //?? do we need that?
    val isLocationBased: Boolean = true,
    val isSearchBarVisible: Boolean = false
)