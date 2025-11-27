package ru.burchik.myweatherapp.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.burchik.myweatherapp.ui.theme.MyWeatherAppTheme


@Preview
@Composable
private fun MainScreenPreview() {
    MyWeatherAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    Column() {
        TopBar()
        Spacer(modifier = Modifier.height(8.dp))
        CurrentWeather()
    }

}

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {}
) {
    Row(modifier
        .padding(4.dp)
        .height(50.dp)) {
        DayLengthDisplay(Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        IconButton({
            onSettingsClick()
        }) {
            Icon(imageVector = Icons.Outlined.Settings, "Settings")
        }
    }
}

@Composable
fun DayLengthDisplay(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LinearProgressIndicator(
            modifier = Modifier
        )
    }

}

@Composable
fun LocationDisplay(
    modifier: Modifier = Modifier,

    ) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text("Location")
        IconButton({}) {
            Icon(Icons.Outlined.Navigation, "By location")
        }
    }

}

@Composable
fun CurrentWeather(modifier: Modifier = Modifier) {

}