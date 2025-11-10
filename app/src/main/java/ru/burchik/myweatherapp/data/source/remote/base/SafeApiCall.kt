package ru.burchik.myweatherapp.data.source.remote.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.burchik.myweatherapp.domain.util.NetworkResult

suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): NetworkResult<T> {
    return try {
        NetworkResult.Success(apiCall())
    } catch (e: Exception) {
        NetworkResult.Error(NetworkErrorHandler.handleException(e))
    }
}

fun <T> safeApiFlow(
    apiCall: suspend () -> T
): Flow<NetworkResult<T>> = flow {
    emit(NetworkResult.Loading)

    try {
        val result = apiCall()
        emit(NetworkResult.Success(result))
    } catch (e: Exception) {
        emit(NetworkResult.Error(NetworkErrorHandler.handleException(e)))
    }
}