package com.tomheidenreich.zephyr.data.source.health

import com.tomheidenreich.zephyr.domain.model.TelemetryReading
import kotlinx.coroutines.flow.Flow

interface TelemetryDataSource {
    fun observeTelemetry(): Flow<TelemetryReading>

    suspend fun clearTelemetry()
}
