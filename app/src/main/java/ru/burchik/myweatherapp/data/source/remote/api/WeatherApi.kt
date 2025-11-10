package ru.burchik.myweatherapp.data.source.remote.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.burchik.myweatherapp.data.source.remote.dto.WeatherResponse

interface WeatherApi {

    //http://api.weatherapi.com/v1/forecast.json?key=fa8b3df74d4042b9aa7135114252304&q=55.7569,37.6151&days=3

    @GET("forecast.json")
    suspend fun getWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 3
    ): WeatherResponse
}