package com.tomheidenreich.zephyr.domain.repository

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.model.LiveMetrics
import kotlinx.coroutines.flow.Flow

interface LiveMetricsRepository {
    fun observeLiveMetrics(): Flow<RepositoryResult<LiveMetrics>>

    suspend fun clearMetrics()
}
