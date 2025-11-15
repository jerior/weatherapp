package ru.burchik.myweatherapp.glance

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import ru.burchik.myweatherapp.MainActivity
import ru.burchik.myweatherapp.R
import ru.burchik.myweatherapp.domain.model.ForecastDay
import ru.burchik.myweatherapp.domain.model.Weather
import ru.burchik.myweatherapp.domain.model.WeatherCondition
import ru.burchik.myweatherapp.domain.repository.WeatherRepository
import ru.burchik.myweatherapp.domain.util.NetworkResult
import timber.log.Timber

class WeatherWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    val stateLocationParam = ActionParameters.Key<String>("location")

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WeatherProviderEntryPoint {
        fun weatherRepository(): WeatherRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        //todo not a state, just weatherData
        var weatherData = Weather(
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

        //val stateLocation =

        weatherRepository.getWeather("Phuket").collect { result ->
            when (result) {
                is NetworkResult.Success -> {
                    weatherData = result.data
                }
                is NetworkResult.Error -> {}
                is NetworkResult.Loading -> {}
            }
        }

        provideContent {
            GlanceTheme {
                WidgetContent(weatherData)
            }
        }
    }

    @Composable
    fun WidgetMicroContent(weatherData: Weather?) {
        Box(
            modifier = GlanceModifier
                .background(ImageProvider(R.drawable.rounded_background))
                .fillMaxSize(),
            //contentAlignment = Alignment.TopCenter
        ) {
            TemperatureDisplay(
                modifier = GlanceModifier
                    .width(50.dp),
                current = weatherData?.temperature ?: -100.0
            )
            ConditionIconDisplay(
                modifier = GlanceModifier.size(62.dp).padding(top = 34.dp, start = 8.dp),
                //modifier = GlanceModifier.size(42.dp).padding(bottom = 4.dp, end = 4.dp),
                condition = weatherData?.condition
            )
        }
    }

    @Composable
    fun WidgetNormalContent(weatherData: Weather?) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                //.background(MaterialTheme.colorScheme.surface) //Android s+
                //.background(ColorProvider(R.color.widget_background)), //Android 12-
                .background(ImageProvider(R.drawable.rounded_background)), //Android 12-
            //.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //WeatherHeader(weatherData.location)
            TemperatureDisplay(
                modifier = GlanceModifier
                    .width(68.dp)
                    .padding(horizontal = 2.dp),
                current = weatherData?.temperature ?: -100.0
            )
            Spacer(modifier = GlanceModifier.width(12.dp))
            ConditionIconDisplay(
                modifier = GlanceModifier.size(60.dp),
                condition = weatherData?.condition
            )
            Spacer(modifier = GlanceModifier.width(18.dp))
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LocationDisplay(
                    city = weatherData?.location ?: "Unknown",
                    country = weatherData?.country ?: "Unknown",
                    onClick = actionStartActivity<MainActivity>(),
                    modifier = GlanceModifier.defaultWeight()
                )
                FewDaysForecastDisplay(
                    modifier = GlanceModifier,
                    //.background(ImageProvider(R.drawable.rounded_background)),
                    forecastDaysList = weatherData?.forecast,
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(weatherData: Weather?) {
        val size = LocalSize.current
        Timber.d("Size: ${size}")
        if (size.width > 100.dp) {
            WidgetNormalContent(weatherData)
        } else {
            WidgetMicroContent(weatherData)
        }
    }

    @Composable
    fun TemperatureDisplay(
        current: Double?,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Row(
            modifier = modifier
                //.background(ImageProvider(R.drawable.rounded_background))
                .clickable(actionRunCallback<RunActivityCallback>(
                    actionParametersOf(
                        stateLocationParam to "Yekaterinburg"
                    )
                )),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${current?.toInt() ?: "--"}",
                style = TextStyle(
                    fontSize = 40.sp,
                    fontFamily = FontFamily.Serif,
                    color = ColorProvider(R.color.widget_text_primary)
                )
            )
            Box {
                Text(
                    modifier = GlanceModifier.padding(top = (-3).dp, start = (-3).dp),
                    text = "°",
                    style = TextStyle(
                        fontSize = 40.sp,
                        fontFamily = FontFamily.Serif,
                        color = ColorProvider(R.color.widget_text_primary)
                    ),
                )
                /*              Text(
                                  text = "C",
                                  style = TextStyle(fontSize = 20.sp),
                                  modifier = GlanceModifier.padding(top = 16.dp, start = 2.dp)
                              )*/
            }
        }
    }

    @Composable
    fun LocationDisplay(
        city: String,
        country: String,
        onClick: Action,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                //.background(MaterialTheme.colorScheme.surface)
                //.background(ImageProvider(R.drawable.rounded_background))
                .padding(top = 4.dp)
                .clickable(
                    onClick
                )
        ) {
            Text(city, modifier = GlanceModifier, style = TextStyle(fontSize = 18.sp,color = ColorProvider(R.color.widget_text_primary)))
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(country, modifier = GlanceModifier, style = TextStyle(fontSize = 14.sp, color = ColorProvider(R.color.widget_text_primary)))
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    fun ConditionIconDisplay(
        condition: WeatherCondition?,
        modifier: GlanceModifier = GlanceModifier
    ) {
        if (condition == null) return
        Image(
            modifier = modifier,
            contentDescription = "An Icon",
            provider = ImageProvider(condition.getDrawableResId()),
            colorFilter = ColorFilter.tint(
                colorProvider = ColorProvider(R.color.widget_text_primary)
            )
        )
    }

    @Composable
    fun FewDaysForecastDisplay(
        forecastDaysList: List<ForecastDay>?,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            //horizontalAlignment = Alignment.CenterHorizontally
        ) {
            forecastDaysList?.forEachIndexed { idx, forecastDay ->
                DayForecastDisplay(
                    forecastDay = forecastDay,
                    modifier = GlanceModifier
                        .padding(horizontal = 4.dp)
                        .width(54.dp)
                    //.background(ImageProvider(R.drawable.rounded_background))
                )
                if (idx < 2) Spacer(modifier = GlanceModifier.width(8.dp))
            }
        }
    }

    @Composable
    fun DayForecastDisplay(
        forecastDay: ForecastDay,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Column(
            modifier.padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Text(forecastDay.date, modifier = GlanceModifier, style = TextStyle(fontSize = 14.sp))
            ConditionIconDisplay(
                modifier = GlanceModifier.size(24.dp),
                condition = forecastDay.condition
            )
            Text(
                "${forecastDay.maxTemp.toInt()}/${forecastDay.minTemp.toInt()}°",
                modifier = GlanceModifier,
                style = TextStyle(fontSize = 14.sp, color = ColorProvider(R.color.widget_text_primary))
            )
        }
    }
}

class RunActivityCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val locationParam = parameters[ActionParameters.Key<String>("location")]

        //Timber.d("Widget update fo location : $locationState = "Yekaterinburg")
        //val locationState = ActionParameters.Key<String>("my-string-key")

        val inputData = Data.Builder()
            .putString(WeatherRefreshWorker.KEY_LOCATION, locationParam)
            .build()
        Timber.d("Widget update fo location :")
        val refreshWorkRequest = OneTimeWorkRequestBuilder<WeatherRefreshWorker>()
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(context).enqueue(refreshWorkRequest)
    }
}
