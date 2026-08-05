package com.tomheidenreich.zephyr.domain.usecase

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.data.repository.fake.FakeHeadingRepository
import com.tomheidenreich.zephyr.data.repository.fake.FakeTelemetryRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Test

class TelemetryStreamUseCaseTest {
    @Test
    fun `merges heading into telemetry reading`() = runBlocking {
        val telemetryRepository = FakeTelemetryRepository()
        val headingRepository = FakeHeadingRepository().apply {
            setHeadingDegrees(235.0)
        }
        val useCase = TelemetryStreamUseCase(
            telemetryRepository = telemetryRepository,
            headingRepository = headingRepository,
        )

        val result = useCase.observeTelemetry().first()
        val telemetry = (result as RepositoryResult.Data).value

        assertEquals(7.2, telemetry.speedMps ?: -1.0, 0.0001)
        assertEquals(235.0, telemetry.headingDegrees ?: -1.0, 0.0001)
        assertEquals(0.0, telemetry.distanceMeters, 0.0001)
    }
}