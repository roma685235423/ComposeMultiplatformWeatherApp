package data.repository

import data.network.ApiService

class WeatherRepository {
    private val apiService = ApiService()

    suspend fun getWeatherByLatLong(lat: Double, long: Double) = apiService.getWeatherByLatLong(lat, long)
    suspend fun getWeatherByCity(city: String) = apiService.getWeatherByCity(city)
}