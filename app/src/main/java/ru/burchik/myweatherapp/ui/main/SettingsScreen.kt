package ru.burchik.myweatherapp.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.burchik.myweatherapp.ui.theme.MyWeatherAppTheme

@Preview
@Composable
private fun SettingsScreenPreview() {
    MyWeatherAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            SettingsScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column() {
        Text("Settings")
        Spacer(modifier = Modifier.height(8.dp))

        Text("Units")
        Spacer(modifier = Modifier.height(8.dp))

        Text("Cities")
        Spacer(modifier = Modifier.height(8.dp))
    }

}