package ru.burchik.myweatherapp.data.source.remote.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.burchik.myweatherapp.data.source.remote.dto.WeatherResponse

interface OpenWeatherApi {

    //https://api.openweathermap.org/data/2.5/forecast?lat=60.00&lon=59.00&appid=ac8e66a6a8e5c7ebc9607739d2391f09

    @GET("forecast")
    suspend fun getWeatherByCity(
        @Query("appid") openWeatherApiKey: String,
        @Query("q") query: String,
        @Query("days") days: Int = 3
    ): WeatherResponse

    @GET("forecast")
    suspend fun getWeatherByLocation(
        @Query("appid") openWeatherApiKey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("days") days: Int = 3
    ): WeatherResponse
}