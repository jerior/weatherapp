package ru.burchik.myweatherapp.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import timber.log.Timber
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationProvider(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = suspendCoroutine { continuation ->
        try {
            // Check permissions
            if (!hasLocationPermission()) {
                Timber.e("Location permission not granted")
                continuation.resume(null)
                return@suspendCoroutine
            }

            // Get last known location first (faster)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        // If no last location, request current location
                        requestCurrentLocation(continuation)
                    }
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception)
                    continuation.resume(null)
                }
        } catch (e: Exception) {
            Timber.e( e)
            continuation.resume(null)
        }
    }

    private fun requestCurrentLocation(continuation: Continuation<Location?>) {
        try {
            val locationRequest = LocationRequest.Builder(
                5000L
            ).build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    continuation.resume(result.lastLocation)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }

            if (hasLocationPermission()) {
                fusedLocationClient.removeLocationUpdates(
                    //locationRequest,
                    callback,
                    //Looper.getMainLooper()
                )

                // Timeout after 15 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    fusedLocationClient.removeLocationUpdates(callback)
                    continuation.resume(null)
                }, 15000)
            }
        } catch (e: Exception) {
            Timber.e(e)
            continuation.resume(null)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}