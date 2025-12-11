package ru.burchik.myweatherapp.ui.common

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.burchik.myweatherapp.R
import ru.burchik.myweatherapp.ui.theme.MyWeatherAppTheme
import java.util.Date

import java.util.Locale

@Composable
fun HourlyForecastScrollableChart(
    forecasts: List<HourlyForecastView>,
    modifier: Modifier = Modifier
) {
    //val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    //font from R for canvas
    val customFontFamily = FontFamily(Font(R.font.amatic_sc_regular))

    val timeFormat = SimpleDateFormat("H:mm", Locale.getDefault())

    val painters = forecasts.map {
        painterResource(id = it.iconResource)
    }

    val scrollState = rememberScrollState()
    val pointWidth = 80f
    val horizontalPadding = 8.dp
    // Вычисляем общую ширину на основе количества точек
    val chartWidth = (forecasts.size * pointWidth).dp

    val verticalPadding = 40f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Canvas(
            modifier = Modifier
                .width(chartWidth)
                .height(150.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            if (forecasts.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            // Spacing between data points
            val spacing = (width - horizontalPadding.toPx() * 2) / (forecasts.size - 1).coerceAtLeast(1)

            val iconSizePx = 20.dp.toPx()
            val chartHeight = height - verticalPadding - (iconSizePx + verticalPadding * 2)

            // Calculate temperature range
            val maxTemp = forecasts.maxOf { it.temperature }
            val minTemp = forecasts.minOf { it.temperature }
            val tempRange = maxTemp - minTemp

            // Calculate points
            val points = forecasts.mapIndexed { index, forecast ->
                val x = horizontalPadding.toPx() + (index * spacing)
                val normalizedTemp = if (tempRange > 0) {
                    (forecast.temperature - minTemp) / tempRange
                } else {
                    0.5
                }
                val y = verticalPadding + chartHeight * (1 - normalizedTemp).toFloat()
                Offset(x, y)
            }

            // Create smooth bezier curve path
            val path = Path()
            if (points.isNotEmpty()) {
                path.moveTo(points[0].x, points[0].y)

                for (i in 1 until points.size) {
                    val p0 = points[i - 1]
                    val p1 = points[i]

                    // Calculate control points for smooth curve
                    val controlX1 = p0.x + (p1.x - p0.x) / 2
                    val controlX2 = p1.x - (p1.x - p0.x) / 2

                    path.cubicTo(
                        controlX1, p0.y,
                        controlX2, p1.y,
                        p1.x, p1.y
                    )
                }
            }

            // Draw smooth temperature curve
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )

            // Draw Dots
            points.forEach { point ->
                val x = point.x
                val y = point.y

                drawCircle(
                    color = primaryColor,
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = onPrimaryColor,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            // Draw labels for each data point
            forecasts.forEachIndexed { index, forecast ->
                val point = points[index]

                // Draw temperature value above the curve
                val tempText = "${forecast.temperature.toInt()}°"
                val tempTextLayout = textMeasurer.measure(
                    text = tempText,
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = primaryColor,
                        fontFamily = customFontFamily,
                        fontWeight = FontWeight.Bold
                    ),

                )
                drawText(
                    textLayoutResult = tempTextLayout,
                    topLeft = Offset(
                        point.x - tempTextLayout.size.width / 2,
                        point.y - tempTextLayout.size.height - 8
                    )
                )

                // Draw hour text at bottom
                val hourText = timeFormat.format(Date(forecast.timestamp * 1000))

                val hourTextLayout = textMeasurer.measure(
                    text = hourText,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = onSurfaceColor.copy(alpha = 0.7f),
                        fontFamily = customFontFamily,
                    )
                )
                drawText(
                    textLayoutResult = hourTextLayout,
                    topLeft = Offset(
                        point.x - hourTextLayout.size.width / 2,
                        height - verticalPadding + 12
                    )
                )

                with(painters[index]) {
                    translate(left = -8f + (spacing * index), top = chartHeight + verticalPadding * 2) {
                        draw(
                            size = Size(iconSizePx, iconSizePx)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun PreviewHourlyForecastScrollableChart() {
    val timestamp = 1764190166L
    val sampleData = listOf(
        HourlyForecastView(timestamp, 22.0, R.drawable.forecast_cloudy_cloud_weather_sun),
        HourlyForecastView(timestamp, 24.0, R.drawable.forecast_weather_cloudy_cloud),
        HourlyForecastView(timestamp, 26.0, R.drawable.thunder_lightening_weather_cloud_storm),
        HourlyForecastView(
            timestamp,
            25.0,
            R.drawable.weather_winter_forecast_snow_christmas_snowflake_cold
        ),
        HourlyForecastView(timestamp, 23.0, R.drawable.sun_forecast_sunset_weather),
        HourlyForecastView(timestamp, 21.0, R.drawable.rain_drop_weather_cloud_forecast),
        HourlyForecastView(timestamp, 19.0, R.drawable.sun_forecast_sunset_weather),
        HourlyForecastView(timestamp, 18.0, R.drawable.forecast_cloudy_cloud_weather_sun)
    )

    MyWeatherAppTheme() {
        Surface() {
            HourlyForecastScrollableChart(forecasts = sampleData)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun PreviewHourlyForecastScrollableSmallChart() {
    val timestamp = 1764190166L
    val sampleData = listOf(
        HourlyForecastView(timestamp, 22.0, R.drawable.forecast_cloudy_cloud_weather_sun),
        HourlyForecastView(timestamp, 24.0, R.drawable.forecast_weather_cloudy_cloud),
        HourlyForecastView(timestamp, 26.0, R.drawable.thunder_lightening_weather_cloud_storm),
        HourlyForecastView(
            timestamp,
            25.0,
            R.drawable.weather_winter_forecast_snow_christmas_snowflake_cold
        ),
    )

    MyWeatherAppTheme() {
        Surface {
            HourlyForecastScrollableChart(forecasts = sampleData)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun PreviewHourlyForecastScrollableBigChart() {
    val timestamp = 1765036829L
    val sampleData = listOf(
        HourlyForecastView(timestamp + 3600, 22.0, R.drawable.forecast_cloudy_cloud_weather_sun),
        HourlyForecastView(timestamp + 3600 * 2, 24.0, R.drawable.forecast_weather_cloudy_cloud),
        HourlyForecastView(timestamp + 3600 * 3, 26.0, R.drawable.thunder_lightening_weather_cloud_storm),
        HourlyForecastView(
            timestamp + 3600 * 4,
            25.0,
            R.drawable.weather_winter_forecast_snow_christmas_snowflake_cold
        ),
        HourlyForecastView(timestamp + 3600 * 5, 23.0, R.drawable.sun_forecast_sunset_weather),
        HourlyForecastView(timestamp + 3600 * 6, 21.0, R.drawable.rain_drop_weather_cloud_forecast),
        HourlyForecastView(timestamp + 3600 * 7, 19.0, R.drawable.sun_forecast_sunset_weather),
        HourlyForecastView(timestamp + 3600 * 8, 18.0, R.drawable.forecast_cloudy_cloud_weather_sun),
        HourlyForecastView(timestamp + 3600 * 9, 23.0, R.drawable.sun_forecast_sunset_weather),
        HourlyForecastView(timestamp + 3600 * 10, 21.0, R.drawable.rain_drop_weather_cloud_forecast),
        HourlyForecastView(timestamp + 3600 * 11, 19.0, R.drawable.sun_forecast_sunset_weather),
        HourlyForecastView(timestamp + 3600 * 12, 18.0, R.drawable.forecast_cloudy_cloud_weather_sun)
    )

    MyWeatherAppTheme() {
        Surface() {
            HourlyForecastScrollableChart(forecasts = sampleData)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun PreviewHourlyForecastScrollableSingleChart() {
    val timestamp = 1765062025L
    //val timestamp = 1765065625L
    val sampleData = listOf(
        HourlyForecastView(timestamp, 22.0, R.drawable.forecast_cloudy_cloud_weather_sun),
        HourlyForecastView(timestamp + 3600, 22.0, R.drawable.forecast_cloudy_cloud_weather_sun),
    )

    MyWeatherAppTheme() {
        Surface() {
            HourlyForecastScrollableChart(forecasts = sampleData, Modifier)
        }
    }
}