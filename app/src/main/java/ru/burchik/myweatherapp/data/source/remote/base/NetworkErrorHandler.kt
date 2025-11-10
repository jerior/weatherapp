package ru.burchik.myweatherapp.data.source.remote.base

import retrofit2.HttpException
import ru.burchik.myweatherapp.domain.util.NetworkError
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorHandler {

    fun handleException(exception: Exception): NetworkError {
        return when (exception) {
            is HttpException -> handleHttpException(exception)
            is SocketTimeoutException -> NetworkError.Timeout
            is UnknownHostException -> NetworkError.NoInternet
            is IOException -> NetworkError.NoInternet
            else -> NetworkError.Unknown(exception)
        }
    }

    private fun handleHttpException(exception: HttpException): NetworkError {
        return when (exception.code()) {
            400 -> NetworkError.ApiError(400, "Invalid request. Please check your input.")
            401 -> NetworkError.ApiError(401, "Authentication failed. Check API key.")
            403 -> NetworkError.ApiError(403, "Access forbidden. Verify API permissions.")
            404 -> NetworkError.ApiError(404, "Location not found. Try another search.")
            429 -> NetworkError.ApiError(429, "Too many requests. Please try again later.")
            in 500..599 -> NetworkError.ServerError
            else -> NetworkError.ApiError(
                exception.code(),
                exception.message() ?: "Unknown error occurred"
            )
        }
    }

    fun getErrorMessage(error: NetworkError): String {
        return when (error) {
            is NetworkError.NoInternet ->
                "No internet connection. Please check your network."
            is NetworkError.Timeout ->
                "Connection timeout. Please try again."
            is NetworkError.ServerError ->
                "Server error. Please try again later."
            is NetworkError.ApiError ->
                error.message
            is NetworkError.Unknown ->
                "An unexpected error occurred: ${error.throwable.localizedMessage}"
        }
    }
}