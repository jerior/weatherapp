package ru.burchik.myweatherapp.ui.weatherOverview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.ArrowDropDownCircle
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.burchik.myweatherapp.R
import ru.burchik.myweatherapp.domain.model.ForecastDay
import ru.burchik.myweatherapp.domain.model.HourlyForecast
import ru.burchik.myweatherapp.domain.util.IconSet
import ru.burchik.myweatherapp.domain.util.WeatherConditionResolver
import ru.burchik.myweatherapp.ui.common.HourlyForecastScrollableChart
import ru.burchik.myweatherapp.ui.common.WeatherIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
            .displayCutoutPadding()
            .verticalScroll(rememberScrollState())
    ) {

        Crossfade(targetState = state.isSearchBarVisible) { isSearchVisible ->
            if (isSearchVisible) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = {
                        viewModel.onEvent(WeatherEvent.UpdateSearchQuery(it))
                    },
                    onQuerySearch = {
                        viewModel.onEvent(WeatherEvent.GetWeatherByQuery(it))
                    },
                    onLocationSearch = {
                        viewModel.onEvent(WeatherEvent.GetWeatherByLocation)
                    }
                )
            } else {
                LocationHeader(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 18.dp),
                    locationString = "${state.weather?.location}, ${state.weather?.country}",
                    isLocationBased = state.isLocationBased,
                    onHeaderClick = {
                        viewModel.onEvent(WeatherEvent.ToggleSearchBarVisibility)
                    }
                )
            }
        }

        if (state.error.isNotEmpty()) {
            ErrorContent(
                error = state.error,
                onRetry = { viewModel.onEvent(WeatherEvent.RetryLastSearch) }
            )
        }

        when {
            state.isLoading -> {
                LoadingContent()
            }

            state.weather != null -> {
                WeatherContent(
                    weatherState = state,
                )
            }

            state.weather == null -> {
                if (state.isLocationBased) {
                    viewModel.onEvent(WeatherEvent.GetWeatherByLocation)
                } else {
                    viewModel.onEvent(WeatherEvent.GetWeatherByQuery(state.lastSearchedLocation))
                }
            }
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
            .padding(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onRetry() }) {
                Icon(Icons.Filled.Refresh, "Retry")
                Text("Retry")
            }
        }
    }
}

@Composable
fun LocationHeader(
    modifier: Modifier = Modifier,
    locationString: String,
    isLocationBased: Boolean,
    onHeaderClick: () -> Unit
) {
    Row(
        modifier = modifier.clickable {
            onHeaderClick()
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = locationString,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        if (isLocationBased) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.ArrowDropDownCircle,
                contentDescription = null
            )
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun WeatherContent(
    weatherState: WeatherState,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        CurrentWeatherCard(weatherState)
        Spacer(modifier = Modifier.height(8.dp))

        // Hourly Forecast Section
        if (weatherState.weather?.hourlyForecast?.isNotEmpty() == true) {
            Spacer(modifier = Modifier.height(16.dp))
            //HourlyForecastSection(hourlyForecast = weatherState.weather.hourlyForecast)
            val hfc = weatherState.weather.hourlyForecast.map {
                ru.burchik.myweatherapp.ui.common.HourlyForecast(
                    timestamp = it.timeEpoch,
                    temperature = it.temperature,
                    iconResource = WeatherConditionResolver(
                        context,
                        iconSet = IconSet.DEFAULT
                    ).resolveIcon(it.condition)
                )
            }

            HourlyForecastScrollableChart(forecasts = hfc)

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        DailyForecastFixedSection(forecast = weatherState.weather?.forecast!!)
        Spacer(modifier = Modifier.height(16.dp))
        LastUpdateInfo(timestamp = weatherState.weather.timestamp)
    }
}

@Composable
fun LastUpdateInfo(
    modifier: Modifier = Modifier,
    timestamp: Long?,
) {
    val ago = timestamp?.let { System.currentTimeMillis() - it }?.div(60 * 1000)?.toInt()
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            fontSize = 8.sp,
            text = "${ago} mins ago",
        )
    }
}

@Composable
fun CurrentWeatherCard(
    weatherState: WeatherState,
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
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WeatherIcon(
                            modifier = Modifier
                                .alpha(0.5f)
                                .size(64.dp),
                            condition = weatherState.weather!!.condition,
                        )
                        Text(
                            text = "${weatherState.weather.temperature.toInt()}°C",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(
                                R.string.feels_like,
                                weatherState.weather.feelsLike.toInt()
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WeatherDetailIconItem(
                            imageVector = Icons.Outlined.WaterDrop,
                            value = "${weatherState.weather?.humidity}%"
                        )
                        Spacer(Modifier.height(8.dp))
                        WeatherDetailIconItem(
                            imageVector = Icons.Outlined.Air,
                            value = stringResource(
                                R.string.wind_speed_km_h,
                                weatherState.weather?.windSpeed!!.toInt()
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherDetailIconItem(imageVector: ImageVector, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = "Humidity",
            Modifier.size(18.dp)
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
                DailyForecastItem(day)
            }
        }
    }
}

@Composable
fun DailyForecastFixedSection(forecast: List<ForecastDay>) {
    Column {
        Text(
            text = stringResource(R.string.three_days_forecast),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            forecast.forEach { day ->
                DailyForecastItem(day, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun HourlyForecastSection(hourlyForecast: List<HourlyForecast>) {
    Column {
        Text(
            text = stringResource(R.string.today),
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
            text = hour.timeEpoch.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        WeatherIcon(
            condition = hour.condition,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${hour.temperature.toInt()}°",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        /*        if (hour.chanceOfRain > 0) {
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
                }*/
    }
}

@Composable
fun DailyForecastItem(day: ForecastDay, modifier: Modifier = Modifier) {
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
            WeatherIcon(
                condition = day.condition,
                modifier = Modifier.size(24.dp)
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
