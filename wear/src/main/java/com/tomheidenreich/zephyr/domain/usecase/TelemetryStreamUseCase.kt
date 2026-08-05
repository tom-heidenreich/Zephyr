package com.tomheidenreich.zephyr.domain.usecase

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.model.TelemetryReading
import com.tomheidenreich.zephyr.domain.repository.HeadingRepository
import com.tomheidenreich.zephyr.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TelemetryStreamUseCase(
    private val telemetryRepository: TelemetryRepository,
    private val headingRepository: HeadingRepository,
) {
    fun observeTelemetry(): Flow<RepositoryResult<TelemetryReading>> {
        return combine(
            telemetryRepository.observeTelemetry(),
            headingRepository.observeHeading(),
        ) { telemetryResult, headingResult ->
            when (telemetryResult) {
                is RepositoryResult.Data -> {
                    val headingDegrees = when (headingResult) {
                        is RepositoryResult.Data -> headingResult.value
                        else -> telemetryResult.value.headingDegrees
                    }

                    RepositoryResult.Data(
                        telemetryResult.value.copy(headingDegrees = headingDegrees),
                        telemetryResult.freshness,
                    )
                }

                is RepositoryResult.Error -> telemetryResult
                RepositoryResult.Loading -> RepositoryResult.Loading
            }
        }
    }
}