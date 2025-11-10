package ru.burchik.myweatherapp.ui.weatherOverview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.burchik.myweatherapp.data.source.remote.base.NetworkErrorHandler
import ru.burchik.myweatherapp.domain.util.NetworkResult
import ru.burchik.myweatherapp.domain.repository.WeatherRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherState())
    val state = _state.asStateFlow()

    init {
        onEvent(WeatherEvent.SearchWeather("Москва"))
    }

    fun onEvent(event: WeatherEvent) {
        when (event) {
            is WeatherEvent.SearchWeather -> {
                searchWeather(event.location)
            }
            is WeatherEvent.UpdateSearchQuery -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            is WeatherEvent.RetryLastSearch -> {
                searchWeather(_state.value.lastSearchedLocation)
            }
        }
    }

    private fun searchWeather(location: String) {
        if (location.isBlank()) {
            _state.update { it.copy(error = "Город или местоположение") }
            return
        }

        repository.getWeather(location)
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