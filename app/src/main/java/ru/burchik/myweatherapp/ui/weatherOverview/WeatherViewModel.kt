package ru.burchik.myweatherapp.ui.weatherOverview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.burchik.myweatherapp.data.preferences.UserPreferencesRepository
import ru.burchik.myweatherapp.data.source.remote.base.NetworkErrorHandler
import ru.burchik.myweatherapp.domain.util.NetworkResult
import ru.burchik.myweatherapp.domain.repository.WeatherRepository
import ru.burchik.myweatherapp.utils.LocationProvider
import ru.burchik.myweatherapp.utils.WeatherSerializer
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherState())
    val state = _state
        .onStart {
            viewModelScope.launch {
                loadPreferences()
                //loadInitialWeather()
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            WeatherState()
        )

    private suspend fun loadPreferences() {
        Timber.d("Loading preferences...")
        preferencesRepository.userPreferences.collect { preferences ->
            _state.update {
                it.copy(
                    isLoading = false,
                    weather = preferences.weatherJson?.let { json -> WeatherSerializer.fromJson(json) },
                    error = "",
                    searchQuery = preferences.lastSearchedLocation,
                    lastSearchedLocation = preferences.lastSearchedLocation,
                    isLocationBased = preferences.isLocationBased,
                    isSearchBarVisible = false
                )
            }
            _state.value.weather?.timestamp?.let {
                if (it < System.currentTimeMillis() - 30 * 60 * 1000){
                    loadInitialWeather()
                }
            }
        }
        Timber.d("Preferences loaded: ${_state.value.isLocationBased}, ${_state.value.lastSearchedLocation}")
    }

    private fun loadInitialWeather() {
        Timber.d("Loading Initial weather data...")
        if (_state.value.isLocationBased) {
            Timber.d("Get by location: ${_state.value.lastSearchedLocation}")
            getWeatherByLocation()
        } else  {
            Timber.d("Get by last searched location: ${_state.value.lastSearchedLocation}")
            getWeatherByQuery(_state.value.lastSearchedLocation)
        }
    }

    fun onEvent(event: WeatherEvent) {
        when (event) {
            is WeatherEvent.GetWeatherByQuery -> {
                getWeatherByQuery(event.location)
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

    private fun getWeatherByLocation() {
        viewModelScope.launch {
            val location = locationProvider.getCurrentLocation()

            if (location == null) {
                _state.update { it.copy(error = "Не удалось определить местопложение") }
                return@launch
            }

            val locationString = "${location.latitude},${location.longitude}"
            repository.getWeather(locationString)
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
                                    isLocationBased = true,
                                    error = ""
                                )
                            }
                            // Save query-based preference
                            preferencesRepository.setWeather(result.data)
                            preferencesRepository.setIsLocationBased(true)
                            preferencesRepository.setLastLocationCoordinates(
                                location.latitude,
                                location.longitude
                            )
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

    private fun getWeatherByQuery(query: String) {
        if (query.isEmpty()) {
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
                                isLocationBased = false,
                                weather = result.data,
                                error = ""
                            )
                        }
                        // Save query-based preference
                        preferencesRepository.setWeather(result.data)
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