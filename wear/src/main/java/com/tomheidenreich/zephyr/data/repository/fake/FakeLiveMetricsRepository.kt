package com.tomheidenreich.zephyr.data.repository.fake

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.model.LiveMetrics
import com.tomheidenreich.zephyr.domain.repository.LiveMetricsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeLiveMetricsRepository : LiveMetricsRepository {
    private val state = MutableStateFlow<RepositoryResult<LiveMetrics>>(
        RepositoryResult.Data(
            LiveMetrics(
                speedMps = 7.2,
                headingDegrees = 90.0,
            )
        )
    )

    override fun observeLiveMetrics(): Flow<RepositoryResult<LiveMetrics>> = state.asStateFlow()

    override suspend fun clearMetrics() {
        state.value = RepositoryResult.Data(LiveMetrics())
    }
}
