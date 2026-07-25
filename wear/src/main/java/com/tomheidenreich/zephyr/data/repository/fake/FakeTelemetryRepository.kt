package com.tomheidenreich.zephyr.data.repository.fake

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.model.TelemetryReading
import com.tomheidenreich.zephyr.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTelemetryRepository : TelemetryRepository {
    private val state = MutableStateFlow<RepositoryResult<TelemetryReading>>(
        RepositoryResult.Data(
            TelemetryReading(
                speedMps = 7.2,
                headingDegrees = 90.0,
            )
        )
    )

    override fun observeTelemetry(): Flow<RepositoryResult<TelemetryReading>> = state.asStateFlow()

    override suspend fun clearTelemetry() {
        state.value = RepositoryResult.Data(TelemetryReading())
    }
}
