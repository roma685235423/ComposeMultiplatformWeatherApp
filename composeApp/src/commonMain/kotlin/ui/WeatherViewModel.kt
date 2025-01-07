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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource

import weatherapp.composeapp.generated.resources.Res
import weatherapp.composeapp.generated.resources.cloud_fill
import weatherapp.composeapp.generated.resources.cloud_moon_bolt_fill
import weatherapp.composeapp.generated.resources.cloud_moon_fill
import weatherapp.composeapp.generated.resources.cloud_moon_rain_fill
import weatherapp.composeapp.generated.resources.cloud_sleet_fill
import weatherapp.composeapp.generated.resources.cloud_snow_fill
import weatherapp.composeapp.generated.resources.cloud_sun_bolt_fill
import weatherapp.composeapp.generated.resources.cloud_sun_fill
import weatherapp.composeapp.generated.resources.cloud_sun_rain_fill
import weatherapp.composeapp.generated.resources.moon_haze_fill
import weatherapp.composeapp.generated.resources.moon_stars_fill
import weatherapp.composeapp.generated.resources.questionmark_circle_dashed
import weatherapp.composeapp.generated.resources.sun_haze_fill
import weatherapp.composeapp.generated.resources.sun_max_fill

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

    fun setLoadingState() {
        _appState.value = AppState.Loading
    }

    fun getIconUrl(iconCode: String): String {
        return "https://openweathermap.org/img/wn/${iconCode}@2x.png"
    }

    fun getImage(weatherCode: String?): DrawableResource {
        return when (weatherCode) {
            "01d" -> Res.drawable.sun_max_fill
            "01n" -> Res.drawable.moon_stars_fill
            "02d" -> Res.drawable.cloud_sun_fill
            "02n" -> Res.drawable.cloud_moon_fill
            "03d", "03n", "04d", "04n" -> Res.drawable.cloud_fill

            "09d", "09n" -> Res.drawable.cloud_sleet_fill

            "10d" -> Res.drawable.cloud_sun_rain_fill
            "10n" -> Res.drawable.cloud_moon_rain_fill

            "11d" -> Res.drawable.cloud_sun_bolt_fill
            "11n" -> Res.drawable.cloud_moon_bolt_fill

            "13d", "13n" -> Res.drawable.cloud_snow_fill

            "50d" -> Res.drawable.sun_haze_fill
            "50n" -> Res.drawable.moon_haze_fill

            else -> Res.drawable.questionmark_circle_dashed
        }
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