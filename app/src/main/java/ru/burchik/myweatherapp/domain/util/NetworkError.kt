package ru.burchik.myweatherapp.domain.util

sealed class NetworkError {
    data object NoInternet : NetworkError()
    data object Timeout : NetworkError()
    data object ServerError : NetworkError()
    data class ApiError(val code: Int, val message: String) : NetworkError()
    data class Unknown(val throwable: Throwable) : NetworkError()
}