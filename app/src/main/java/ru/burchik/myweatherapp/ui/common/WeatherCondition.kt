package ru.burchik.myweatherapp.ui.common

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import ru.burchik.myweatherapp.domain.model.WeatherCondition
import ru.burchik.myweatherapp.domain.util.IconSet
import ru.burchik.myweatherapp.domain.util.WeatherConditionResolver

@Composable
fun WeatherConditionText(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val context = LocalContext.current
    val resolver = remember { WeatherConditionResolver(context) }

    Text(
        text = resolver.getLocalizedName(condition),
        modifier = modifier,
        style = style
    )
}

@Composable
fun WeatherIcon(
    modifier: Modifier = Modifier,
    condition: WeatherCondition,
    iconSet: IconSet = IconSet.DEFAULT,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val resolver = remember(iconSet) {
        WeatherConditionResolver(context, iconSet)
    }

    val iconRes = resolver.resolveIcon(condition)
    val description = contentDescription ?: resolver.getLocalizedName(condition)

    Image(
        painter = painterResource(id = iconRes),
        contentDescription = description,
        modifier = modifier
    )
}