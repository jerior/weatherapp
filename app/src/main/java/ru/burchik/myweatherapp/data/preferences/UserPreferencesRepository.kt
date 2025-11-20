package ru.burchik.myweatherapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to create DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val IS_LOCATION_BASED = booleanPreferencesKey("is_location_based")
        private val LAST_SEARCHED_LOCATION = stringPreferencesKey("last_searched_location")
        private val LAST_LOCATION_LATITUDE = doublePreferencesKey("last_location_latitude")
        private val LAST_LOCATION_LONGITUDE = doublePreferencesKey("last_location_longitude")
    }

    // Flow to read preferences
    val userPreferences: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            UserPreferences(
                isLocationBased = preferences[IS_LOCATION_BASED] ?: true,
                lastSearchedLocation = preferences[LAST_SEARCHED_LOCATION] ?: "",
                lastLocationLatitude = preferences[LAST_LOCATION_LATITUDE],
                lastLocationLongitude = preferences[LAST_LOCATION_LONGITUDE]
            )
        }.onEach {
            Timber.d("UserPreferences: ${it}")
        }

    // Save location-based preference
    suspend fun setIsLocationBased(isLocationBased: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOCATION_BASED] = isLocationBased
        }
    }

    // Save last searched location (city name or query)
    suspend fun setLastSearchedLocation(location: String) {
        dataStore.edit { preferences ->
            preferences[LAST_SEARCHED_LOCATION] = location
        }
    }

    // Save last GPS coordinates
    suspend fun setLastLocationCoordinates(latitude: Double, longitude: Double) {
        dataStore.edit { preferences ->
            preferences[LAST_LOCATION_LATITUDE] = latitude
            preferences[LAST_LOCATION_LONGITUDE] = longitude
        }
    }

    // Clear all preferences
    suspend fun clearPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

// Data class to hold user preferences
data class UserPreferences(
    val isLocationBased: Boolean = true,
    val lastSearchedLocation: String = "",
    val lastLocationLatitude: Double? = null,
    val lastLocationLongitude: Double? = null
)