package com.tomheidenreich.zephyr.domain.usecase

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.repository.WindRepository
import com.tomheidenreich.zephyr.domain.wind.WindObservation
import kotlinx.coroutines.flow.Flow

class WindStreamUseCase(
    private val windRepository: WindRepository,
) {
    fun observeCurrentWind(spotId: String): Flow<RepositoryResult<WindObservation>> {
        return windRepository.observeCurrentWind(spotId)
    }
}