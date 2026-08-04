package com.tomheidenreich.zephyr.data.source.weather

import com.tomheidenreich.zephyr.domain.weather.WeatherLocation

interface WeatherLocationProvider {
    suspend fun getCurrentLocation(): WeatherLocation?
}
