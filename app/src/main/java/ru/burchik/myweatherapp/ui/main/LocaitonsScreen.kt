package ru.burchik.myweatherapp.ui.main

import android.content.res.Resources
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.burchik.myweatherapp.ui.theme.MyWeatherAppTheme

@Preview
@Composable
private fun LocationsScreenPreview() {
    MyWeatherAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            LocationsScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun LocationsScreen(modifier: Modifier = Modifier) {
    Column() {
        Text("Manage cities")
        Spacer(modifier = Modifier.height(8.dp))

        Text("Units")
        Spacer(modifier = Modifier.height(8.dp))
    }
}