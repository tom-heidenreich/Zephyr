package com.tomheidenreich.zephyr.data.source.health

import com.tomheidenreich.zephyr.domain.model.LiveMetrics
import kotlinx.coroutines.flow.Flow

interface LiveMetricsDataSource {
    fun observeLiveMetrics(): Flow<LiveMetrics>
}
