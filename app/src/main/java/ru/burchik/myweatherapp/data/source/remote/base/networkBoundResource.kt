/*
package ru.burchik.myweatherapp.data.source.remote.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

fun <ResultType, RequestType> networkBoundResource(
    query: () -> Flow<ResultType>,
    fetch: suspend () -> RequestType,
    saveFetchResult: suspend (RequestType) -> Unit,
    shouldFetch: (ResultType) -> Boolean = { true },
    onFetchSuccess: () -> Unit = { },
    onFetchFailed: (Throwable) -> Unit = { }
) = channelFlow {
    val data = query().first()

    //val flow =
    if (shouldFetch(data)) {

        //emit(Resource.Loading(data))
        val loading = launch {
            query().collect { send(Resource.Loading) }
        }

        try {
            saveFetchResult(fetch())
            onFetchSuccess()
            loading.cancel()
            //query().map { Resource.Success(it) }
            query().collect { send(Resource.Success(it)) }
        } catch (t: Throwable) {
            onFetchFailed(t)
            //query().map { Resource.Error(throwable, it) }
            loading.cancel()
            query().collect { send(Resource.Error(t.message ?: "Error", t)) }
        }
    } else {
        //query().map { Resource.Success(it) }
        query().collect { send(Resource.Success(it)) }
    }
    //emitAll(flow)
}*/
