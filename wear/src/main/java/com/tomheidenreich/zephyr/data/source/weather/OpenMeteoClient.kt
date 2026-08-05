package com.tomheidenreich.zephyr.data.source.weather

import com.tomheidenreich.zephyr.domain.weather.WeatherLocation
import com.tomheidenreich.zephyr.domain.weather.WeatherSnapshot

interface OpenMeteoClient {
    suspend fun fetchWeather(location: WeatherLocation): WeatherSnapshot
}
