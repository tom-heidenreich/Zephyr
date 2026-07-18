package com.tomheidenreich.zephyr.data.repository.health

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.data.source.health.HealthServicesExerciseBridge
import com.tomheidenreich.zephyr.domain.model.LiveMetrics
import com.tomheidenreich.zephyr.domain.repository.LiveMetricsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class HealthServicesLiveMetricsRepository(
    private val bridge: HealthServicesExerciseBridge,
) : LiveMetricsRepository {
    override fun observeLiveMetrics(): Flow<RepositoryResult<LiveMetrics>> {
        return bridge.observeLiveMetrics()
            .map { metrics -> metrics?.let { RepositoryResult.Data(it) } ?: RepositoryResult.Loading }
            .catch { emit(RepositoryResult.Error(it)) }
    }

    override suspend fun clearMetrics() {
        bridge.clearMetrics()
    }
}