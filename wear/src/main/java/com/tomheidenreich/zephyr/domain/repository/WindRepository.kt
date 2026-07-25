package com.tomheidenreich.zephyr.domain.repository

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.wind.WindForecast
import com.tomheidenreich.zephyr.domain.wind.WindObservation
import kotlinx.coroutines.flow.Flow

interface WindRepository {
    fun observeCurrentWind(spotId: String): Flow<RepositoryResult<WindObservation>>

    fun observeForecast(spotId: String): Flow<RepositoryResult<WindForecast>>

    suspend fun refresh(spotId: String)
}
