package ru.burchik.myweatherapp.domain.util

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val error: NetworkError) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}