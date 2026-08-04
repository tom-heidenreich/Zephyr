package com.tomheidenreich.zephyr.data.source.weather

import com.tomheidenreich.zephyr.domain.weather.WeatherLocation
import com.tomheidenreich.zephyr.domain.weather.WeatherSnapshot
import com.tomheidenreich.zephyr.domain.weather.WeatherSourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class HttpOpenMeteoClient(
    private val requestBuilder: OpenMeteoWeatherRequestBuilder = OpenMeteoWeatherRequestBuilder(),
) : OpenMeteoClient {
    override suspend fun fetchWeather(location: WeatherLocation): WeatherSnapshot = withContext(Dispatchers.IO) {
        val request = requestBuilder.build(location)
        val response = execute(request.url)
        OpenMeteoWeatherResponseParser.parse(location, response)
    }

    private fun execute(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            ?: throw IllegalStateException("Open-Meteo request failed with HTTP $code")

        val body = BufferedReader(InputStreamReader(stream)).use { reader -> reader.readText() }
        if (code !in 200..299) {
            throw IllegalStateException("Open-Meteo request failed with HTTP $code: $body")
        }
        return body
    }
}

internal object OpenMeteoWeatherResponseParser {
    fun parse(location: WeatherLocation, json: String): WeatherSnapshot {
        val weatherObject = extractObject(json, "minutely_15")
            ?: throw IllegalStateException("Open-Meteo response missing minutely_15 data")

        val time = firstStringArrayValue(weatherObject, "time")
            ?: throw IllegalStateException("Open-Meteo response missing minutely_15 time")
        val observedAt = OffsetDateTime.parse(time + "Z").toInstant()

        return WeatherSnapshot(
            location = location,
            observedAt = Instant.now(),
            validFrom = observedAt,
            validUntil = observedAt.plusSeconds(15 * 60),
            sourceType = WeatherSourceType.FORECAST,
            source = "open-meteo",
            temperatureC = firstDoubleArrayValue(weatherObject, "temperature_2m"),
            apparentTemperatureC = firstDoubleArrayValue(weatherObject, "apparent_temperature"),
            relativeHumidityPercent = firstIntArrayValue(weatherObject, "relative_humidity_2m"),
            windSpeedKts = firstDoubleArrayValue(weatherObject, "wind_speed_10m"),
            windDirectionDegrees = firstIntArrayValue(weatherObject, "wind_direction_10m"),
            windGustKts = firstDoubleArrayValue(weatherObject, "wind_gusts_10m"),
            precipitationMm = firstDoubleArrayValue(weatherObject, "precipitation"),
            weatherCode = firstIntArrayValue(weatherObject, "weather_code"),
        )
    }

    private fun extractObject(json: String, key: String): String? {
        val pattern = Regex("\"$key\"\\s*:\\s*\\{(.*?)\\}", setOf(RegexOption.DOT_MATCHES_ALL))
        return pattern.find(json)?.groupValues?.getOrNull(1)
    }

    private fun firstStringArrayValue(json: String, key: String): String? = extractArray(json, key)?.firstOrNull()

    private fun firstDoubleArrayValue(json: String, key: String): Double? =
        extractArray(json, key)?.firstOrNull()?.toDoubleOrNull()

    private fun firstIntArrayValue(json: String, key: String): Int? =
        extractArray(json, key)?.firstOrNull()?.toIntOrNull()

    private fun extractArray(json: String, key: String): List<String>? {
        val pattern = Regex("\"$key\"\\s*:\\s*\\[(.*?)\\]", setOf(RegexOption.DOT_MATCHES_ALL))
        val rawValues = pattern.find(json)?.groupValues?.getOrNull(1) ?: return null
        if (rawValues.isBlank()) return emptyList()
        return rawValues.split(',').map { it.trim().trim('"') }
    }
}
