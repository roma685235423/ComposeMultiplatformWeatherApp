package org.example.weatherapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.geo.compose.LocationTrackerAccuracy
import dev.icerock.moko.geo.compose.rememberLocationTrackerFactory
import dev.icerock.moko.permissions.PermissionState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ui.AppState
import ui.WeatherViewModel

import weatherapp.composeapp.generated.resources.Res
import weatherapp.composeapp.generated.resources.icons8_doorbell
import weatherapp.composeapp.generated.resources.icons8_hygrometer
import weatherapp.composeapp.generated.resources.icons8_wind

@Composable
@Preview
fun App() {
    MaterialTheme {
        val factory = rememberLocationTrackerFactory(LocationTrackerAccuracy.Best)
        val locationTracker = remember { factory.createLocationTracker() }
        val viewModel = viewModel { WeatherViewModel(locationTracker) }
        val cityToSearch = remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()

        BindLocationTrackerEffect(locationTracker)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = CenterHorizontally
        ) {
            val permissionState = viewModel.permissionState.collectAsState()
            val appState = viewModel.appState.collectAsState()

            when (permissionState.value) {
                PermissionState.Granted -> {
                    LaunchedEffect(Unit) {
                        viewModel.updateLocationData()
                    }
                    when (appState.value) {
                        is AppState.Error -> {
                            val message = (appState.value as AppState.Error).message
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(text = message)
                            }
                        }
                        AppState.Loading -> {
                            CircularProgressIndicator()
                            Text(text = "Loading...")
                        }
                        is AppState.Success -> {
                            val data = (appState.value as AppState.Success).data
                            val textId = data.weather?.getOrNull(0)?.icon
                           Box(
                               modifier = Modifier.fillMaxSize().background(
                                   brush = Brush.verticalGradient(
                                       colors = listOf(
                                           Color(0xFF000000),
                                           Color(0xff4A90E2)
                                       )
                                   )
                               ).systemBarsPadding()
                           )
                           {
                               Spacer(modifier = Modifier.size(16.dp))
                               Column(
                                   modifier = Modifier
                                       .fillMaxWidth()
                                       .padding(16.dp)
                               ) {
                                   Row(
                                       horizontalArrangement = Arrangement.SpaceBetween,
                                       modifier = Modifier
                                           .fillMaxWidth()
                                   ) {
                                       Text(text = data.name ?: "N/A", color = Color.White)
                                       Icon(
                                           painter = painterResource(Res.drawable.icons8_doorbell),
                                           contentDescription = null,
                                           tint = Color.White,
                                           modifier = Modifier.size(24.dp)
                                       )
                                   }
                                   Spacer(modifier = Modifier.size(16.dp))
                                   Row(
                                       horizontalArrangement = Arrangement.SpaceBetween,
                                       modifier = Modifier.fillMaxWidth()
                                   ) {
                                       OutlinedTextField(
                                           value = cityToSearch.value,
                                           onValueChange = {
                                               cityToSearch.value = it
                                           },
                                           colors = TextFieldDefaults.textFieldColors(
                                               backgroundColor = Color.White.copy(alpha = .1f),
                                               cursorColor = Color.White,
                                               textColor = Color.White,
                                               focusedIndicatorColor = Color.White,
                                               unfocusedIndicatorColor = Color.Transparent
                                           )
                                       )
                                       IconButton(
                                           onClick = {
                                               coroutineScope.launch {
                                                   viewModel.setLoadingState()
                                                   viewModel.updateLocationData()
                                               }
                                           }
                                       ) {
                                           Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color.White)
                                       }
                                       IconButton(
                                           onClick = {
                                               viewModel.searchByCity(cityToSearch.value)
                                               cityToSearch.value = ""
                                           }
                                       ) {
                                           Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.White)
                                       }
                                   }

                                   Column(
                                       modifier = Modifier.fillMaxWidth(),
                                       horizontalAlignment = CenterHorizontally
                                   ) {

                                   Spacer(modifier = Modifier.size(16.dp))

                                   Image(
                                       painter = painterResource(
                                           viewModel.getImage(
                                               data.weather?.getOrNull(0)?.icon ?: ""
                                           )
                                       ),
                                       contentDescription = null,
                                       modifier = Modifier.size(120.dp)
                                   )
                                       Spacer(modifier = Modifier.size(6.dp))
                                   Text(text = data.name ?: "N/A",
                                       style = MaterialTheme.typography.h6.copy(
                                           color = Color.White,
                                           fontWeight = FontWeight.Bold
                                       )
                                   )

                                       Spacer(modifier = Modifier.size(16.dp))

                                   Column(
                                       horizontalAlignment = CenterHorizontally,
                                       verticalArrangement = Arrangement.SpaceBetween,
                                       modifier = Modifier
                                           .padding(16.dp)
                                           .fillMaxWidth()
                                           .clip(RoundedCornerShape(16.dp))
                                           .background(color = Color.White.copy(alpha = .1f))
                                           .padding(16.dp)
                                   ) {
                                       val tempText = data.main?.temp?.toInt()?.toString() ?: "N/A"
                                       Text(
                                           text = viewModel.kelvinToCelsiusString(data.main?.temp),
                                           style = MaterialTheme.typography.h2.copy(
                                               color = Color.White,
                                               fontWeight = FontWeight.ExtraBold
                                           ),
                                           fontSize = 54.sp
                                       )

                                       Spacer(modifier = Modifier.size(16.dp))

                                       Text(
                                           text = data.weather?.getOrNull(0)?.description ?: "",
                                           style = MaterialTheme.typography.h6.copy(
                                               color = Color.White,
                                               fontWeight = FontWeight.Bold
                                           )
                                       )
                                       Spacer(modifier = Modifier.size(16.dp))

                                       WeatherCard(
                                           image = Res.drawable.icons8_wind,
                                           title = "Wind",
                                           value = "${data.wind?.speed} m/s"
                                       )
                                       WeatherCard(
                                           image = Res.drawable.icons8_hygrometer,
                                           title = "Humiduty",
                                           value = "${data.main?.humidity} %"
                                       )
                                   }
                                   }
                               }
                           }
                        }
                    }
                }
                PermissionState.DeniedAlways -> {
                    Button(onClick = { locationTracker.permissionsController.openAppSettings() }) {
                        Text(text = "Open App Settings")
                    }
                }
                else -> {
                    Button(onClick = { viewModel.provideLocationPermission() }) {
                        Text(text = "Grant Permission")
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherCard(image: DrawableResource, title: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = title, color = Color.White)
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = ":", color = Color.White)
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = value, color = Color.White)
    }
}