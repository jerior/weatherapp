package ru.burchik.myweatherapp.glance

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.Button
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
import androidx.glance.currentState
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
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ru.burchik.myweatherapp.MainActivity
import ru.burchik.myweatherapp.R
import ru.burchik.myweatherapp.domain.model.ForecastDay
import ru.burchik.myweatherapp.domain.model.Weather
import ru.burchik.myweatherapp.domain.model.WeatherCondition
import ru.burchik.myweatherapp.utils.WeatherSerializer
import timber.log.Timber

class WeatherWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<Preferences> = WeatherStateDefinition

    val stateLocationParam = ActionParameters.Key<String>("location")

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
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
    private fun WidgetContent(/*weatherData: Weather?*/) {
        val prefs = currentState<Preferences>()

        val weatherJson = prefs[WeatherPrefsKeys.WEATHER_JSON]
        val errorMessage = prefs[WeatherPrefsKeys.ERROR_MESSAGE]
        val lastUpdate = prefs[WeatherPrefsKeys.LAST_UPDATE] ?: 0L
        val lastLocation = prefs[WeatherPrefsKeys.LOCATION] ?: "Yekaterinburg"

        Timber.d("weatherJson: ${weatherJson}")

        if (weatherJson == null) {
            if (lastUpdate < System.currentTimeMillis() - 60 * 60 * 1000) {
                actionRunCallback<RunActivityCallback>(
                    actionParametersOf(
                        stateLocationParam to lastLocation,
                    )
                )
            }
            ErrorView(errorMessage ?: "Failed to load weather data")
            return
        }

        val weatherData = WeatherSerializer.fromJson(weatherJson)
        if (weatherData != null) {
            //WeatherDetailsView(weather, lastUpdate)
            val size = LocalSize.current
            if (size.width > 100.dp) {
                WidgetNormalContent(weatherData)
            } else {
                WidgetMicroContent(weatherData)
            }
        } else {
            ErrorView("Failed to load weather data")
        }
    }

    @Composable
    private fun ErrorView(message: String) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = TextStyle(
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            )
            //Spacer(modifier = GlanceModifier.height(4.dp))
            Row() {
                Text(
                    text = "⚠️",
                    style = TextStyle(fontSize = 32.sp)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Button(
                    text = "Retry",
                    onClick = actionRunCallback<RefreshAction>()
                )
            }

        }
    }

    @SuppressLint("RestrictedApi")
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
                        stateLocationParam to "Yekaterinburg" //todo
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
        Timber.d("Widget update fo location :")
        val refreshWorkRequest = OneTimeWorkRequestBuilder<WeatherUpdateWorker>()
            .build()
        WorkManager.getInstance(context).enqueue(refreshWorkRequest)
    }
}
