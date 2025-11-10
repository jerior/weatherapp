package ru.burchik.myweatherapp.ui.theme.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.burchik.myweatherapp.domain.util.WeatherIconMapper

@Composable
fun WeatherIconByCondition(
    condition: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isDay: Boolean = true,
    withBackground: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    val icon = WeatherIconMapper.getIconByCondition(condition, isDay)

    if (withBackground) {
        Box(
            modifier = modifier
                .size(size + 16.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = condition,
                modifier = Modifier.size(size),
                tint = tint
            )
        }
    } else {
        Icon(
            imageVector = icon,
            contentDescription = condition,
            modifier = modifier.size(size),
            tint = tint
        )
    }
}