package com.tomheidenreich.zephyr.data.source.weather

import com.tomheidenreich.zephyr.domain.weather.WeatherCellSelection
import com.tomheidenreich.zephyr.domain.weather.WeatherLocation
import com.tomheidenreich.zephyr.domain.weather.WeatherSourceType
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenMeteoWeatherResponseParserTest {
    @Test
    fun `parses the first minutely fifteen values into a weather snapshot`() {
        val location = WeatherLocation(
            latitude = 52.52,
            longitude = 13.41,
            timezone = "Europe/Berlin",
            cellSelection = WeatherCellSelection.NEAREST,
        )
        val json = """
            {
              "minutely_15": {
                "time": ["2026-08-04T10:00", "2026-08-04T10:15"],
                "temperature_2m": [18.5, 18.8],
                "apparent_temperature": [19.0, 19.2],
                "relative_humidity_2m": [68, 66],
                "wind_speed_10m": [12.0, 13.0],
                "wind_direction_10m": [270, 275],
                "wind_gusts_10m": [17.0, 18.0],
                "precipitation": [0.0, 0.2],
                "weather_code": [0, 3]
              }
            }
        """.trimIndent()

        val snapshot = OpenMeteoWeatherResponseParser.parse(location, json)

        assertEquals(location, snapshot.location)
        assertEquals(WeatherSourceType.FORECAST, snapshot.sourceType)
        assertEquals(18.5, snapshot.temperatureC ?: -1.0, 0.0001)
        assertEquals(19.0, snapshot.apparentTemperatureC ?: -1.0, 0.0001)
        assertEquals(68, snapshot.relativeHumidityPercent)
        assertEquals(12.0, snapshot.windSpeedKts ?: -1.0, 0.0001)
        assertEquals(270, snapshot.windDirectionDegrees)
        assertEquals(17.0, snapshot.windGustKts ?: -1.0, 0.0001)
        assertEquals(0.0, snapshot.precipitationMm ?: -1.0, 0.0001)
        assertEquals(0, snapshot.weatherCode)
    }
}
