package ru.burchik.myweatherapp.glance

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import ru.burchik.myweatherapp.R
import ru.burchik.myweatherapp.domain.model.Weather
import ru.burchik.myweatherapp.domain.model.WeatherCondition
import ru.burchik.myweatherapp.domain.repository.WeatherRepository
import ru.burchik.myweatherapp.domain.util.NetworkResult

class WeatherWidget : GlanceAppWidget() {

    // a way to get hilt inject what you need in non-suported class
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WeatherProviderEntryPoint {
        fun weatherRepository(): WeatherRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {

//get settings repository from hilt
        var weatherState = Weather(
            location = "Moscow",
            country = "Russia",
            temperature = 0.0,
            condition = WeatherCondition.ClearSky,
            humidity = 0,
            windSpeed = 0.0,
            feelsLike = 0.0,
            hourlyForecast = listOf(),
            forecast = listOf()
        )

        val appContext = context.applicationContext ?: throw IllegalStateException()
        val statisticsEntryPoint =
            EntryPointAccessors.fromApplication(
                appContext,
                WeatherProviderEntryPoint::class.java,
            )
        val weatherRepository = statisticsEntryPoint.weatherRepository()

        //set value
/*        withContext(Dispatchers.IO) {
        }*/

        weatherRepository.getWeather("Antalya").collect { result ->
            when (result) {
                is NetworkResult.Success -> {
                    weatherState = result.data
                }
                else -> {}
            }
        }

        provideContent {
            WidgetContent(weatherState)
        }
    }

    @Composable
    private fun WidgetContent(weatherData: Weather?) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp)
        ) {
            //WeatherHeader(weatherData.location)
            TemperatureDisplay(
                current = weatherData?.temperature ?: -100.0
                //current = 5.0
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            ConditionIconDisplay(weatherData?.condition)
            Spacer(modifier = GlanceModifier.width(8.dp))
            LocationDisplay(
                city = weatherData?.location ?: "Unknown",
                country = weatherData?.country ?: "Unknown",
                modifier = GlanceModifier.defaultWeight()
            )
            Spacer(modifier = GlanceModifier.width(8.dp))

        }
    }

    @Composable
    fun TemperatureDisplay(current: Double?) {
        Text(text = "${current?.toInt() ?: "--"}°", style = TextStyle(fontSize = 42.sp))
        Text(text = "C", style = TextStyle(fontSize = 24.sp))
    }

    @Composable
    fun LocationDisplay(
        city: String,
        country: String,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Column (
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp)
        ) {
            Text(city, modifier = GlanceModifier, style = TextStyle(fontSize = 20.sp))
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(country, modifier = GlanceModifier, style = TextStyle(fontSize = 14.sp))
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    fun IconDisplay() {
        Image(
            contentDescription = "An Icon",
            provider = ImageProvider(R.drawable.forecast_weather_cloudy_cloud),
        )
    }

    @SuppressLint("RestrictedApi")
    @Composable
    fun ConditionIconDisplay(condition: WeatherCondition?) {
        if (condition == null) return
        Image(
            contentDescription = "An Icon",
            provider = ImageProvider(condition.getDrawableResId()),
        )
    }
}
