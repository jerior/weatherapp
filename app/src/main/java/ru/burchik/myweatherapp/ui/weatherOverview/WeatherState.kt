package ru.burchik.myweatherapp.ui.weatherOverview

import ru.burchik.myweatherapp.domain.model.Weather

data class WeatherState(
    val isLoading: Boolean = true,
    val weather: Weather? = null,
    val error: String = "",
    val searchQuery: String = "",
    val lastSearchedLocation: String = "", //?? do we need that?
    val isLocationBased: Boolean = false,
    val isSearchBarVisible: Boolean = false
)