package ru.burchik.myweatherapp.ui.weatherOverview

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.updateAll
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import ru.burchik.myweatherapp.R
import ru.burchik.myweatherapp.domain.model.ForecastDay
import ru.burchik.myweatherapp.domain.model.HourlyForecast
import ru.burchik.myweatherapp.domain.model.Weather
import ru.burchik.myweatherapp.glance.WeatherWidget
import ru.burchik.myweatherapp.ui.theme.common.WeatherIconByCondition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasLocationPermission by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (hasLocationPermission) {
            getCurrentLocation(context) { location ->
                viewModel.onEvent(WeatherEvent.GetWeatherByLocation(location))
            }
            scope.launch {
                WeatherWidget().updateAll(context)
            }
        }
    }

    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission) {
            getCurrentLocation(context) { location ->
                viewModel.onEvent(WeatherEvent.GetWeatherByLocation(location))
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        WeatherWidget().updateAll(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.isSearchBarVisible) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = {
                        viewModel.onEvent(WeatherEvent.UpdateSearchQuery(it))
                    },
                    onQuerySearch = {
                        viewModel.onEvent(WeatherEvent.GetWeatherByQuery(it))
                    },
                    onLocationSearch = {
                        if (hasLocationPermission) {
                            getCurrentLocation(context) { location ->
                                viewModel.onEvent(WeatherEvent.GetWeatherByLocation(location))
                                scope.launch {
                                    WeatherWidget().updateAll(context)
                                }
                            }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )
            }

            when {
                state.isLoading -> {
                    LoadingContent()
                }

                state.error.isNotEmpty() -> {
                    ErrorContent(
                        error = state.error,
                        onRetry = { viewModel.onEvent(WeatherEvent.RetryLastSearch) }
                    )
                }

                state.weather != null -> {
                    WeatherContent(
                        weatherState = state,
                        onHeaderClick = {
                            viewModel.onEvent(WeatherEvent.ToggleSearchBarVisibility)
                        }
                    )
                }

                state.weather == null -> {
                    if (state.isLocationBased) {
                        getCurrentLocation(context) { location ->
                            viewModel.onEvent(WeatherEvent.GetWeatherByLocation(location))
                        }
                    } else {
                        viewModel.onEvent(WeatherEvent.GetWeatherByQuery(state.lastSearchedLocation))
                    }

                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(context: android.content.Context, onLocationReceived: (String) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener {
        if (it != null) {
            onLocationReceived("${it.latitude},${it.longitude}")
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onQuerySearch: (String) -> Unit,
    onLocationSearch: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Город или местоположение") },
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            trailingIcon = {
                Row() {
                    IconButton(
                        onClick = { onQuerySearch(query) },
                        modifier = Modifier
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(
                        onClick = { onLocationSearch() },
                        modifier = Modifier
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Search"
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Загрузка...")
        }
    }
}

@Composable
fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                modifier = Modifier.size(48.dp),
                contentDescription = "Alert"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onRetry() }) {
                Icon(Icons.Filled.Refresh, "Retry")
                Text("Retry")
            }
        }
    }
}

@Composable
fun IconSwitchButton() {
    var checked by remember { mutableStateOf(false) }

    Switch(
        checked = checked,
        onCheckedChange = { checked = it },
        thumbContent = {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Checked"
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.TextFields,
                    contentDescription = "Unchecked"
                )
            }
        }
    )
}

@Composable
fun WeatherContent(
    weatherState: WeatherState,
    onHeaderClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        CurrentWeatherCard(weatherState, onHeaderClick = onHeaderClick)
        Spacer(modifier = Modifier.height(8.dp))
        WeatherDetailsCard(weatherState.weather!!)

        // Add Hourly Forecast Section
        if (weatherState.weather.hourlyForecast.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            HourlyForecastSection(hourlyForecast = weatherState.weather.hourlyForecast)
        }

        Spacer(modifier = Modifier.height(16.dp))
        ForecastFixedSection(forecast = weatherState.weather.forecast)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/*@Preview
@Composable
private fun CurrentWeatherCardPreview() {
    CurrentWeatherCard(
        weather = Weather(
            location = "Москва",
            country = "Россия",
            temperature = 25.0,
            condition = WeatherCondition.ClearSky,
            humidity = 87,
            windSpeed = 5.0,
            feelsLike = 7.4,
            hourlyForecast = listOf(),
            forecast = listOf()
        )
    )
}*/

@Composable
fun CurrentWeatherCard(
    weatherState: WeatherState,
    onHeaderClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.clickable {
                        onHeaderClick()
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${weatherState.weather?.location}, ${weatherState.weather?.country}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    if (weatherState.isLocationBased) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null
                        )
                    }

                }
                Spacer(modifier = Modifier.height(24.dp))
                /*                Text(
                                    text = "${weather.temperature.toInt()}°C",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Bold
                                )*/
                WeatherIconByCondition(
                    modifier = Modifier.alpha(0.5f),
                    condition = weatherState.weather!!.condition,
                    size = 64.dp
                )
                Text(
                    text = "${weatherState.weather.temperature.toInt()}°C",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                /*                Text(
                                    text = weather.condition,
                                    style = MaterialTheme.typography.titleLarge
                                )*/
                Text(
                    //text = "Ощущается как ${weather.feelsLike.toInt()}°C",
                    text = stringResource(
                        R.string.feels_like,
                        weatherState.weather.feelsLike.toInt()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WeatherDetailsCard(weather: Weather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WeatherDetailItem(
                label = "Влажность",
                value = "${weather.humidity}%"
            )
            HorizontalDivider(
                modifier = Modifier
                    .height(60.dp)
                    .width(1.dp)
            )
            WeatherDetailItem(
                label = "Ветер",
                value = "${weather.windSpeed.toInt()} км/ч"
            )
        }
    }
}

@Composable
fun WeatherDetailItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ForecastSection(forecast: List<ForecastDay>) {
    Column {
        Text(
            text = "3-Day Forecast",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(forecast) { day ->
                ForecastCard(day)
            }
        }
    }
}

@Composable
fun ForecastFixedSection(forecast: List<ForecastDay>) {
    Column {
        Text(
            text = "На 3 дня",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            forecast.forEach { day ->
                ForecastCard(day, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun HourlyForecastSection(hourlyForecast: List<HourlyForecast>) {
    Column {
        Text(
            text = "Сегодня",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            LazyRow(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(hourlyForecast) { hour ->
                    HourlyForecastItem(hour)
                }
            }
        }
    }
}

@Composable
fun HourlyForecastItem(hour: HourlyForecast) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Text(
            text = hour.time,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        WeatherIconByCondition(
            condition = hour.condition,
            size = 24.dp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${hour.temperature.toInt()}°",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (hour.chanceOfRain > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = "Umbrella",
                    Modifier.size(18.dp)
                )
                Text(
                    text = "${hour.chanceOfRain}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ForecastCard(day: ForecastDay, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(140.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.date,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            WeatherIconByCondition(
                condition = day.condition,
                size = 24.dp
            )
            /*            Text(
                            text = day.condition,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )*/
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${day.maxTemp.toInt()}° / ${day.minTemp.toInt()}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = "Umbrella",
                    Modifier.size(18.dp)
                )
                Text(
                    text = "${day.chanceOfRain}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

        }
    }
}
