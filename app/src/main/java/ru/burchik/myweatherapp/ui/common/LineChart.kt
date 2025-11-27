package ru.burchik.myweatherapp.ui.common

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.burchik.myweatherapp.R
import ru.burchik.myweatherapp.ui.theme.MyWeatherAppTheme
import java.util.Date

import java.util.Locale


data class HourlyForecast(
    val timestamp: Long,
    val temperature: Double,
    //val icon: String // e.g., "☀️", "☁️", "🌧️"
    val iconResource: Int,
)

/*data class HourlyForecast(
    val time: String,
    val temperature: Double,
    val condition: WeatherCondition,
    val chanceOfRain: Int,
    val windSpeed: Double
)*/

@Composable
fun HourlyForecastChart(
    forecasts: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    //val hourFormatter = DateTimeFormatter.ofPattern("H:mm")
    val timeFormat = SimpleDateFormat("H:mm", Locale.getDefault())

    val painters = forecasts.map {
        painterResource(id = it.iconResource)
    }


    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (forecasts.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val horizontalPadding = 10f
            val verticalPadding = 40f
            val chartHeight = height - verticalPadding * 2
            val chartWidth = width - horizontalPadding * 2

            // Calculate temperature range
            val maxTemp = forecasts.maxOf { it.temperature }
            val minTemp = forecasts.minOf { it.temperature }
            val tempRange = maxTemp - minTemp

            // Spacing between data points
            val spacing = chartWidth / (forecasts.size - 1).coerceAtLeast(1)

            // Draw grid lines
            for (i in 0..3) {
                val y = verticalPadding + (chartHeight / 3) * i
                drawLine(
                    color = onSurfaceColor.copy(alpha = 0.05f),
                    start = Offset(horizontalPadding, y),
                    end = Offset(width - horizontalPadding, y),
                    strokeWidth = 1f
                )
            }

            // Calculate points
            val points = forecasts.mapIndexed { index, forecast ->
                val x = horizontalPadding + index * spacing
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

            // Draw labels for each data point
            forecasts.forEachIndexed { index, forecast ->
                val point = points[index]

                // Draw temperature value above the curve
                val tempText = "${forecast.temperature.toInt()}°"
                val tempTextLayout = textMeasurer.measure(
                    text = tempText,
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = primaryColor
                    )
                )
                drawText(
                    textLayoutResult = tempTextLayout,
                    topLeft = Offset(
                        point.x - tempTextLayout.size.width / 2,
                        point.y - tempTextLayout.size.height - 8
                    )
                )

                // Draw hour text at bottom
                //val hourText = forecast.timestamp.format(hourFormatter)
                val hourText = timeFormat.format(Date(forecast.timestamp * 1000))

                val hourTextLayout = textMeasurer.measure(
                    text = hourText,
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = onSurfaceColor.copy(alpha = 0.7f)
                    )
                )
                drawText(
                    textLayoutResult = hourTextLayout,
                    topLeft = Offset(
                        point.x - hourTextLayout.size.width / 2,
                        height - verticalPadding + 12
                    )
                )

                // Draw weather icon below hour
/*                val iconTextLayout = textMeasurer.measure(
                    text = forecast.icon,
                    style = TextStyle(fontSize = 16.sp)
                )
                drawText(
                    textLayoutResult = iconTextLayout,
                    topLeft = Offset(
                        point.x - iconTextLayout.size.width / 2,
                        height - verticalPadding + 56
                    )
                )*/
                //index * 138f -15f

                val iconSizePx = 20.dp.toPx()
                val space = ((width - horizontalPadding -8f)/ (painters.size - 1))
                with(painters[index]){
                    translate(left = -15f + (space * index) , top = 300f){
                        draw(
                            size = Size(iconSizePx, iconSizePx)
                        )
                    }
                }

            }
        }
    }
}

/*    val sampleData = listOf(
        HourlyForecast(now(), 22f, "☀️"),
        HourlyForecast(now().plusHours(1), 24f, "☀️"),
        HourlyForecast(now().plusHours(2), 26f, "⛅"),
        HourlyForecast(now().plusHours(3), 25f, "☁️"),
        HourlyForecast(now().plusHours(4), 23f, "🌧️"),
        HourlyForecast(now().plusHours(5), 21f, "🌧️"),
        HourlyForecast(now().plusHours(6), 19f, "⛅"),
        HourlyForecast(now().plusHours(7), 18f, "🌙")
    )*/

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun PreviewHourlyForecastChart() {
    val timestamp = 1764190166L
    val sampleData = listOf(
        HourlyForecast(timestamp, 22.0, R.drawable.forecast_cloudy_cloud_weather_sun),
        HourlyForecast(timestamp, 24.0, R.drawable.forecast_weather_cloudy_cloud),
        HourlyForecast(timestamp, 26.0, R.drawable.thunder_lightening_weather_cloud_storm),
        HourlyForecast(timestamp, 25.0, R.drawable.weather_winter_forecast_snow_christmas_snowflake_cold),
        HourlyForecast(timestamp, 23.0, R.drawable.sun_forecast_sunset_weather),
        HourlyForecast(timestamp, 21.0, R.drawable.rain_drop_weather_cloud_forecast),
        HourlyForecast(timestamp, 19.0, R.drawable.sun_forecast_sunset_weather),
        HourlyForecast(timestamp, 18.0, R.drawable.forecast_cloudy_cloud_weather_sun)
    )

    MyWeatherAppTheme() {
        Surface() {
            HourlyForecastChart(forecasts = sampleData, Modifier.height(150.dp))
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun PreviewHourlyForecastChart2() {
    val timestamp = 1764190166L
    val sampleData = listOf(
        HourlyForecast(timestamp, 22.0, R.drawable.forecast_cloudy_cloud_weather_sun),
        HourlyForecast(timestamp, 24.0, R.drawable.forecast_weather_cloudy_cloud),
        HourlyForecast(timestamp, 26.0, R.drawable.thunder_lightening_weather_cloud_storm),
        HourlyForecast(timestamp, 25.0, R.drawable.weather_winter_forecast_snow_christmas_snowflake_cold),
    )

    MyWeatherAppTheme() {
        Surface {
            HourlyForecastChart(forecasts = sampleData, Modifier.height(150.dp))
        }
    }
}