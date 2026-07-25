package com.tomheidenreich.zephyr.domain.repository

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.model.TelemetryReading
import kotlinx.coroutines.flow.Flow

interface TelemetryRepository {
    fun observeTelemetry(): Flow<RepositoryResult<TelemetryReading>>

    suspend fun clearTelemetry()
}