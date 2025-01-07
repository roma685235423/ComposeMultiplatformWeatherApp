package ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.models.WeatherResponse
import data.repository.WeatherRepository
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import io.ktor.util.valuesOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val locationTracker: LocationTracker
): ViewModel() {

    private val _permissionState = MutableStateFlow(PermissionState.NotDetermined)
    val permissionState = _permissionState.asStateFlow()

    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState = _appState.asStateFlow()

    private val repository = WeatherRepository()


    init {
        viewModelScope.launch {
            _permissionState.value = locationTracker.permissionsController.getPermissionState(
                Permission.LOCATION
            )
        }
    }

    fun provideLocationPermission() {
        viewModelScope.launch {
            val isGranted =
                locationTracker.permissionsController.isPermissionGranted(Permission.LOCATION)

            if (isGranted) {
                return@launch
            }

            try {
                locationTracker.permissionsController.providePermission(Permission.LOCATION)
                _permissionState.value = PermissionState.Granted
            } catch (e: DeniedException) {
                _permissionState.value = PermissionState.Denied
                println("✅✅✅" + e)
            } catch (e: DeniedAlwaysException) {
                _permissionState.value = PermissionState.DeniedAlways
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchWeather(lat: Double, long: Double) {
        viewModelScope.launch {
            _appState.value = AppState.Loading
            try {
                val result = repository.getWeatherByLatLong(lat, long)
                _appState.value = AppState.Success(result)
            } catch (e: Exception) {
                _appState.value = AppState.Error(e.message.toString())
                e.printStackTrace()
            }
        }
    }

    fun searchByCity(city: String) {
        viewModelScope.launch {
            _appState.value = AppState.Loading
            try {
                val result = repository.getWeatherByCity(city)
                _appState.value = AppState.Success(result)
            } catch (e: Exception) {
                _appState.value = AppState.Error(e.message.toString())
                e.printStackTrace()
            }
        }
    }

    fun kelvinToCelsiusString(kelvin: Double?): String {
        return kelvin?.let {
            val intValueMultipleTo10 = ((it - 273.15)*10).toInt()
            val resultValue = intValueMultipleTo10.toDouble() / 10
            if (resultValue % 1 == 0.0) {
                "${intValueMultipleTo10.toInt() / 10} °C"
            } else {
                "${intValueMultipleTo10.toDouble() / 10} °C"
            }
        } ?: "N/A"
    }

    suspend fun updateLocationData() {
        locationTracker.startTracking()
        val location = locationTracker.getLocationsFlow().first()
        locationTracker.stopTracking()
        fetchWeather(location.latitude, location.longitude)
    }
}

sealed class AppState {
    object Loading: AppState()
    data class Success(val data: WeatherResponse): AppState()
    data class Error(val message: String): AppState()
}