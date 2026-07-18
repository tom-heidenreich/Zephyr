package com.tomheidenreich.zephyr.domain.usecase

import com.tomheidenreich.zephyr.domain.model.LiveMetrics
import com.tomheidenreich.zephyr.domain.model.PointOfSail
import com.tomheidenreich.zephyr.domain.model.WindDataSourceType
import com.tomheidenreich.zephyr.domain.model.WindObservation
import com.tomheidenreich.zephyr.domain.model.WindReference
import com.tomheidenreich.zephyr.domain.model.WindSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class DeriveSailingMetricsUseCaseTest {
    private val useCase = DeriveSailingMetricsUseCase()

    @Test
    fun `uses observed true wind when available`() {
        val input = LiveMetrics(speedMps = 6.0, headingDegrees = 90.0)
        val observedTrue = WindSample(speedKts = 20.0, directionFromDegrees = 270, reference = WindReference.TRUE)
        val observedApparent =
            WindSample(speedKts = 24.0, directionFromDegrees = 250, reference = WindReference.APPARENT)
        val observation = WindObservation(
            spotId = "spot-a",
            observedAt = Instant.parse("2026-01-01T00:00:00Z"),
            trueWind = observedTrue,
            apparentWind = observedApparent,
            sourceType = WindDataSourceType.FUSED,
            source = "test",
        )

        val result = useCase(input, observation)

        assertEquals(observedTrue, result.trueWind)
        assertEquals(observedApparent, result.apparentWind)
        assertEquals(180.0, result.pointOfSailAngleDegrees ?: -1.0, 0.0001)
        assertEquals(PointOfSail.RUNNING, result.pointOfSail)
    }

    @Test
    fun `derives true wind from apparent wind and boat motion`() {
        val input = LiveMetrics(speedMps = 5.0, headingDegrees = 45.0)
        val observation = WindObservation(
            spotId = "spot-b",
            observedAt = Instant.parse("2026-01-01T00:00:00Z"),
            trueWind = null,
            apparentWind = WindSample(speedKts = 18.0, directionFromDegrees = 270, reference = WindReference.APPARENT),
            sourceType = WindDataSourceType.INSTRUMENT,
            source = "test",
        )

        val result = useCase(input, observation)

        assertNotNull(result.trueWind)
        assertEquals(WindReference.TRUE, result.trueWind?.reference)
        assertNotNull(result.trueWind?.directionFromDegrees)
        assertNotNull(result.pointOfSailAngleDegrees)
        assertNotNull(result.pointOfSail)
    }

    @Test
    fun `computes exact angle and close hauled classification at threshold`() {
        val input = LiveMetrics(speedMps = 6.0, headingDegrees = 0.0)
        val observation = WindObservation(
            spotId = "spot-c",
            observedAt = Instant.parse("2026-01-01T00:00:00Z"),
            trueWind = WindSample(speedKts = 16.0, directionFromDegrees = 35, reference = WindReference.TRUE),
            apparentWind = null,
            sourceType = WindDataSourceType.API,
            source = "test",
        )

        val result = useCase(input, observation)

        assertEquals(35.0, result.pointOfSailAngleDegrees ?: -1.0, 0.0001)
        assertEquals(PointOfSail.CLOSE_HAULED, result.pointOfSail)
    }

    @Test
    fun `returns null angle and point of sail when heading or wind direction is missing`() {
        val inputMissingHeading = LiveMetrics(speedMps = 6.0, headingDegrees = null)
        val observationMissingDirection = WindObservation(
            spotId = "spot-d",
            observedAt = Instant.parse("2026-01-01T00:00:00Z"),
            trueWind = WindSample(speedKts = 12.0, directionFromDegrees = null, reference = WindReference.TRUE),
            apparentWind = null,
            sourceType = WindDataSourceType.API,
            source = "test",
        )

        val result = useCase(inputMissingHeading, observationMissingDirection)

        assertNull(result.pointOfSailAngleDegrees)
        assertNull(result.pointOfSail)
    }

    @Test
    fun `normalizes wrap around headings for exact angle`() {
        val input = LiveMetrics(speedMps = 6.0, headingDegrees = 350.0)
        val observation = WindObservation(
            spotId = "spot-e",
            observedAt = Instant.parse("2026-01-01T00:00:00Z"),
            trueWind = WindSample(speedKts = 14.0, directionFromDegrees = 10, reference = WindReference.TRUE),
            apparentWind = null,
            sourceType = WindDataSourceType.API,
            source = "test",
        )

        val result = useCase(input, observation)

        assertEquals(20.0, result.pointOfSailAngleDegrees ?: -1.0, 0.0001)
        assertEquals(PointOfSail.IN_IRONS, result.pointOfSail)
    }
}
