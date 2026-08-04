package com.tomheidenreich.zephyr.domain.usecase

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.repository.WeatherRepository
import com.tomheidenreich.zephyr.domain.weather.WeatherSnapshot
import kotlinx.coroutines.flow.Flow

class WeatherStreamUseCase(
    private val weatherRepository: WeatherRepository,
) {
    fun observeWeather(): Flow<RepositoryResult<WeatherSnapshot>> {
        return weatherRepository.observeWeather()
    }

    suspend fun refreshWeather() {
        weatherRepository.refresh()
    }
}
