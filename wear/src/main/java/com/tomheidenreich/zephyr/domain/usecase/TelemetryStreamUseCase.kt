package com.tomheidenreich.zephyr.domain.usecase

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.model.TelemetryReading
import com.tomheidenreich.zephyr.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow

class TelemetryStreamUseCase(
    private val telemetryRepository: TelemetryRepository,
) {
    fun observeTelemetry(): Flow<RepositoryResult<TelemetryReading>> {
        return telemetryRepository.observeTelemetry()
    }
}