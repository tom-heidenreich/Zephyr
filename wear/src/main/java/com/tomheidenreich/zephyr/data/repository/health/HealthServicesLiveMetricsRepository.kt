package com.tomheidenreich.zephyr.data.repository.health

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.data.source.health.HealthServicesExerciseBridge
import com.tomheidenreich.zephyr.domain.model.TelemetryReading
import com.tomheidenreich.zephyr.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class HealthServicesTelemetryRepository(
    private val bridge: HealthServicesExerciseBridge,
) : TelemetryRepository {
    override fun observeTelemetry(): Flow<RepositoryResult<TelemetryReading>> {
        return bridge.observeTelemetry()
            .map { metrics -> metrics?.let { RepositoryResult.Data(it) } ?: RepositoryResult.Loading }
            .catch { emit(RepositoryResult.Error(it)) }
    }

    override suspend fun clearTelemetry() {
        bridge.clearTelemetry()
    }
}