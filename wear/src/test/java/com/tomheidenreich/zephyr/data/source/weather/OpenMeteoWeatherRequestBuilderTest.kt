package com.tomheidenreich.zephyr.data.source.weather

import com.tomheidenreich.zephyr.domain.weather.WeatherCellSelection
import com.tomheidenreich.zephyr.domain.weather.WeatherLocation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class OpenMeteoWeatherRequestBuilderTest {
    private val builder = OpenMeteoWeatherRequestBuilder()

    @Test
    fun `requests only minutely fifteen weather data for four hours ahead`() {
        val request = builder.build(
            WeatherLocation(
                latitude = 52.52,
                longitude = 13.41,
                timezone = "Europe/Berlin",
                cellSelection = WeatherCellSelection.SEA,
            ),
            now = Instant.parse("2026-08-04T10:07:00Z"),
        )

        assertTrue(request.url.contains("minutely_15=temperature_2m,apparent_temperature,relative_humidity_2m,wind_speed_10m,wind_direction_10m,wind_gusts_10m,precipitation,weather_code"))
        assertTrue(request.url.contains("forecast_minutely_15=16"))
        assertTrue(request.url.contains("cell_selection=sea"))
        assertTrue(request.url.contains("timezone=Europe%2FBerlin"))
        assertFalse(request.url.contains("past_"))
    }
}
