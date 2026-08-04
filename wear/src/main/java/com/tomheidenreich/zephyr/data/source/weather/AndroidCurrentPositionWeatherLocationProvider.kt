package com.tomheidenreich.zephyr.data.source.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.tomheidenreich.zephyr.domain.weather.WeatherCellSelection
import com.tomheidenreich.zephyr.domain.weather.WeatherLocation
import java.util.TimeZone

class AndroidCurrentPositionWeatherLocationProvider(
    context: Context,
) : WeatherLocationProvider {
    private val appContext = context.applicationContext
    private val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override suspend fun getCurrentLocation(): WeatherLocation? {
        if (!hasLocationPermission()) return null

        val location = listOfNotNull(
            getLastKnown(LocationManager.GPS_PROVIDER),
            getLastKnown(LocationManager.NETWORK_PROVIDER),
            getLastKnown(LocationManager.PASSIVE_PROVIDER),
        ).maxByOrNull { it.time } ?: return null

        return WeatherLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            timezone = TimeZone.getDefault().id,
            cellSelection = WeatherCellSelection.NEAREST,
        )
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
