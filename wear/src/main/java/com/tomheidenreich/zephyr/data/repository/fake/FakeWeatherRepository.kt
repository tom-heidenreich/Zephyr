package com.tomheidenreich.zephyr.data.repository.fake

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.repository.WeatherRepository
import com.tomheidenreich.zephyr.domain.weather.WeatherCellSelection
import com.tomheidenreich.zephyr.domain.weather.WeatherLocation
import com.tomheidenreich.zephyr.domain.weather.WeatherSnapshot
import com.tomheidenreich.zephyr.domain.weather.WeatherSourceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

class FakeWeatherRepository : WeatherRepository {
    private val weatherState = MutableStateFlow<RepositoryResult<WeatherSnapshot>>(RepositoryResult.Loading)

    override fun observeWeather(): Flow<RepositoryResult<WeatherSnapshot>> {
        if (weatherState.value == RepositoryResult.Loading) {
            val now = Instant.now()
            weatherState.value = RepositoryResult.Data(
                WeatherSnapshot(
                    location = WeatherLocation(
                        latitude = 52.52,
                        longitude = 13.41,
                        timezone = "Europe/Berlin",
                        cellSelection = WeatherCellSelection.NEAREST,
                    ),
                    observedAt = now,
                    validFrom = now,
                    validUntil = now.plusSeconds(4 * 60 * 60),
                    sourceType = WeatherSourceType.FORECAST,
                    source = "fake",
                    temperatureC = 18.5,
                    apparentTemperatureC = 19.0,
                    relativeHumidityPercent = 68,
                    windSpeedKts = 18.0,
                    windDirectionDegrees = 270,
                    windGustKts = 22.0,
                    precipitationMm = 0.0,
                    weatherCode = 0,
                    cloudCoverPercent = 18,
                    pressureMslHpa = 1016.0,
                )
            )
        }
        return weatherState.asStateFlow()
    }

    override suspend fun refresh() {
        observeWeather()
    }
}
