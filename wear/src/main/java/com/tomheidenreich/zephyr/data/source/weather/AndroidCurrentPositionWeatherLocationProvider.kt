package com.tomheidenreich.zephyr.data.source.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import com.tomheidenreich.zephyr.domain.weather.WeatherCellSelection
import com.tomheidenreich.zephyr.domain.weather.WeatherLocation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import java.util.TimeZone

class AndroidCurrentPositionWeatherLocationProvider(
    context: Context,
) : WeatherLocationProvider {
    private val appContext = context.applicationContext
    private val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override suspend fun getCurrentLocation(): WeatherLocation? {
        if (!hasLocationPermission()) return null

        val location = try {
            withContext(Dispatchers.Main.immediate) {
                requestFreshLocation(LocationManager.GPS_PROVIDER)
                    ?: requestFreshLocation(LocationManager.NETWORK_PROVIDER)
                    ?: getLastKnown(LocationManager.GPS_PROVIDER)
                    ?: getLastKnown(LocationManager.NETWORK_PROVIDER)
                    ?: getLastKnown(LocationManager.PASSIVE_PROVIDER)
            }
        } catch (securityException: SecurityException) {
            return null
        } ?: return null

        return WeatherLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            timezone = TimeZone.getDefault().id,
            cellSelection = WeatherCellSelection.NEAREST,
        )
    }

    private suspend fun requestFreshLocation(provider: String): Location? {
        if (!locationManager.isProviderEnabled(provider)) return null

        val request = LocationRequest.Builder(Duration.ofSeconds(10).toMillis())
            .setQuality(LocationRequest.QUALITY_HIGH_ACCURACY)
            .build()

        val signal = CancellationSignal()
        return try {
            withTimeoutOrNull(12_000) {
                val result = CompletableDeferred<Location?>()
                val executor = appContext.mainExecutor
                locationManager.getCurrentLocation(
                    provider,
                    request,
                    signal,
                    executor,
                    java.util.function.Consumer { location -> result.complete(location) },
                )
                result.await()
            }
        } catch (securityException: SecurityException) {
            null
        }
    }

    private fun getLastKnown(provider: String): Location? {
        if (!locationManager.isProviderEnabled(provider)) return null
        return runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
    }

    private fun hasLocationPermission(): Boolean {
        val coarseGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val fineGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return coarseGranted || fineGranted
    }
}
