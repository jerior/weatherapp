package ru.burchik.myweatherapp.ui.weatherOverview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.burchik.myweatherapp.data.preferences.UserPreferencesRepository
import ru.burchik.myweatherapp.data.source.remote.base.NetworkErrorHandler
import ru.burchik.myweatherapp.domain.util.NetworkResult
import ru.burchik.myweatherapp.domain.repository.WeatherRepository
import ru.burchik.myweatherapp.utils.LocationProvider
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherState())
    val state = _state.asStateFlow()

    init {
        // Load preferences when ViewModel is created
        loadPreferences()
    }

    private fun loadPreferences() {
        Timber.d("Loading preferences...")
        viewModelScope.launch {
            preferencesRepository.userPreferences.collect { preferences ->
                _state.update {
                    it.copy(
                        isLocationBased = preferences.isLocationBased,
                        lastSearchedLocation = preferences.lastSearchedLocation
                    )
                }
            }
            Timber.d("Preferences loaded: ${_state.value.isLocationBased}, ${_state.value.lastSearchedLocation}")
        }
    }

    fun onEvent(event: WeatherEvent) {
        when (event) {
            is WeatherEvent.LoadInitialWeather -> {
                loadInitialWeather()
            }
            is WeatherEvent.GetWeatherByQuery -> {
                getWeatherByQuery(event.location)
                _state.update { it.copy(lastSearchedLocation = event.location, searchQuery = event.location, isLocationBased = false) }
            }
            is WeatherEvent.GetWeatherByLocation -> {
                getWeatherByLocation()
            }
            is WeatherEvent.UpdateSearchQuery -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            is WeatherEvent.RetryLastSearch -> {
                if (_state.value.isLocationBased) {
                    getWeatherByLocation()
                    return
                }
                getWeatherByQuery(_state.value.lastSearchedLocation)
            }
            is WeatherEvent.ToggleLocationBasedSearch -> {
                _state.update { it.copy(isLocationBased = !_state.value.isLocationBased) }
            }
            is WeatherEvent.ToggleSearchBarVisibility -> {
                _state.update { it.copy(isSearchBarVisible = !_state.value.isSearchBarVisible) }
            }
        }
    }

    private fun loadInitialWeather() {
        viewModelScope.launch {
            // Wait for preferences to load
            preferencesRepository.userPreferences.first().let { preferences ->
                if (preferences.isLocationBased) {
                    getWeatherByLocation()
                } else if (preferences.lastSearchedLocation.isNotEmpty()) {
                    Timber.d("Last searched location: ${preferences.lastSearchedLocation}")
                    getWeatherByQuery(preferences.lastSearchedLocation)
                } else {
                    // Default: try to get current location
                    getWeatherByLocation()
                }
            }
        }
    }

    private fun getWeatherByLocation() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = "") }

            try {
                val location = locationProvider.getCurrentLocation()
                if (location != null) {
                    val locationString = "${location.latitude},${location.longitude}"
                    // Save location-based preference
                    preferencesRepository.setIsLocationBased(true)
                    preferencesRepository.setLastLocationCoordinates(
                        location.latitude,
                        location.longitude
                    )

                    getWeatherByQuery(locationString)

                    //getWeatherByLocation(locationString)
                    _state.update {
                        it.copy(
                            isLocationBased = true,
                            lastSearchedLocation = locationString
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Не удалось получить местоположение. Проверьте разрешения."
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка получения местоположения: ${e.message}"
                    )
                }
            }
        }
    }

    private fun getWeatherByQuery(query: String) {
        if (query.isNullOrBlank()) {
            _state.update { it.copy(error = "В запросе нет данных о местопложении") }
            return
        }

        repository.getWeather(query)
            .onEach { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = true,
                                error = ""
                            )
                        }
                    }
                    is NetworkResult.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                weather = result.data,
                                error = ""
                            )
                        }
                        // Save query-based preference
                        preferencesRepository.setIsLocationBased(false)
                        preferencesRepository.setLastSearchedLocation(query)
                    }
                    is NetworkResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = NetworkErrorHandler.getErrorMessage(result.error)
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}