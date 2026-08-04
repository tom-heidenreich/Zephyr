package com.tomheidenreich.zephyr.domain.weather

import java.time.Instant

data class WeatherLocation(
    val latitude: Double,
    val longitude: Double,
    val timezone: String = "auto",
    val cellSelection: WeatherCellSelection = WeatherCellSelection.NEAREST,
)

enum class WeatherCellSelection {
    LAND,
    SEA,
    NEAREST,
}

enum class WeatherSourceType {
    LIVE,
    FORECAST,
    CACHED,
}

data class WeatherSnapshot(
    val location: WeatherLocation,
    val observedAt: Instant,
    val validFrom: Instant,
    val validUntil: Instant,
    val sourceType: WeatherSourceType,
    val source: String,
    val temperatureC: Double? = null,
    val apparentTemperatureC: Double? = null,
    val relativeHumidityPercent: Int? = null,
    val windSpeedKts: Double? = null,
    val windDirectionDegrees: Int? = null,
    val windGustKts: Double? = null,
    val precipitationMm: Double? = null,
    val weatherCode: Int? = null,
    val cloudCoverPercent: Int? = null,
    val pressureMslHpa: Double? = null,
)
