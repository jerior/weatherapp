package ru.burchik.myweatherapp.data.source.remote.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.burchik.myweatherapp.domain.util.NetworkResult

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