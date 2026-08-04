package com.tomheidenreich.zephyr.data.source.weather

import com.tomheidenreich.zephyr.domain.weather.WeatherCellSelection
import com.tomheidenreich.zephyr.domain.weather.WeatherLocation
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit

data class OpenMeteoWeatherRequest(
    val url: String,
)

class OpenMeteoWeatherRequestBuilder {
    fun build(location: WeatherLocation, now: Instant = Instant.now()): OpenMeteoWeatherRequest {
        val hourlyVariables = listOf(
            "temperature_2m",
            "apparent_temperature",
            "relative_humidity_2m",
            "wind_speed_10m",
            "wind_direction_10m",
            "wind_gusts_10m",
            "precipitation",
            "weather_code",
        ).joinToString(",")

        val query = buildList {
            add("latitude=${location.latitude}")
            add("longitude=${location.longitude}")
            add("minutely_15=$hourlyVariables")
            add("forecast_minutely_15=16")
            add("timezone=${encode(location.timezone)}")
            add("cell_selection=${location.cellSelection.toQueryValue()}")
        }.joinToString("&")

        return OpenMeteoWeatherRequest(
            url = "https://api.open-meteo.com/v1/forecast?$query",
        )
    }

    private fun WeatherCellSelection.toQueryValue(): String = when (this) {
        WeatherCellSelection.LAND -> "land"
        WeatherCellSelection.SEA -> "sea"
        WeatherCellSelection.NEAREST -> "nearest"
    }

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
