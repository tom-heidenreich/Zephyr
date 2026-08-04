package com.tomheidenreich.zephyr.domain.repository

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.weather.WeatherSnapshot
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun observeWeather(): Flow<RepositoryResult<WeatherSnapshot>>

    suspend fun refresh()
}
