package com.tomheidenreich.zephyr.domain.usecase

import com.tomheidenreich.zephyr.domain.model.TelemetryReading
import com.tomheidenreich.zephyr.domain.sailing.PointOfSail
import com.tomheidenreich.zephyr.domain.wind.WindReference
import com.tomheidenreich.zephyr.domain.weather.WeatherCellSelection
import com.tomheidenreich.zephyr.domain.weather.WeatherLocation
import com.tomheidenreich.zephyr.domain.weather.WeatherSnapshot
import com.tomheidenreich.zephyr.domain.weather.WeatherSourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class DeriveSailingMetricsUseCaseTest {
    private val useCase = DeriveSailingMetricsUseCase()

    @Test
    fun `uses weather wind values when available`() {
        val input = TelemetryReading(speedMps = 10.0, headingDegrees = 60.0)
        val weather = weatherSnapshot(
            windSpeedKts = 20.0,
            windDirectionDegrees = 300,
        )

        val result = useCase(input, weather)

        assertEquals(WindReference.TRUE, result.trueWind?.reference)
        assertEquals(20.0, result.trueWind?.speedKts ?: -1.0, 0.0001)
        assertEquals(120.0, result.pointOfSailAngleDegrees ?: -1.0, 0.0001)
        assertEquals(PointOfSail.BROAD_REACH, result.pointOfSail)
    }

    @Test
    fun `computes velocity made good and efficiency from weather wind`() {
        val input = TelemetryReading(speedMps = 10.0, headingDegrees = 60.0)
        val weather = weatherSnapshot(
            windSpeedKts = 20.0,
            windDirectionDegrees = 300,
        )

        val result = useCase(input, weather)

        assertEquals(
            5.0,
            result.velocityMadeGoodMps ?: -1.0,
            0.0001
        )
        assertEquals(0.485961123, result.velocityMadeGoodEfficiency ?: -1.0, 0.000000001)
        assertEquals(120.0, result.pointOfSailAngleDegrees ?: -1.0, 0.0001)
        assertEquals(19.7, result.apparentWind?.speedKts ?: -1.0, 0.1)
        assertEquals(359, result.apparentWind?.directionFromDegrees)
        assertEquals(61.0, result.apparentWindAngleDegrees ?: -1.0, 1.0)
    }

    @Test
    fun `returns null velocity made good efficiency when weather wind speed is zero`() {
        val input = TelemetryReading(speedMps = 10.0, headingDegrees = 45.0)
        val weather = weatherSnapshot(
            windSpeedKts = 0.0,
            windDirectionDegrees = 270,
        )

        val result = useCase(input, weather)

        assertEquals(
            7.071067812,
            result.velocityMadeGoodMps ?: -1.0,
            0.000000001
        )
        assertNull(
            result.velocityMadeGoodEfficiency,
        )
    }

    @Test
    fun `computes exact angle and close hauled classification at threshold`() {
        val input = TelemetryReading(speedMps = 6.0, headingDegrees = 0.0)
        val weather = weatherSnapshot(windSpeedKts = 16.0, windDirectionDegrees = 35)

        val result = useCase(input, weather)

        assertEquals(35.0, result.pointOfSailAngleDegrees ?: -1.0, 0.0001)
        assertEquals(PointOfSail.CLOSE_HAULED, result.pointOfSail)
    }

    @Test
    fun `returns null angle and point of sail when heading or wind direction is missing`() {
        val inputMissingHeading = TelemetryReading(speedMps = 6.0, headingDegrees = null)
        val weatherMissingDirection = weatherSnapshot(windSpeedKts = 12.0, windDirectionDegrees = null)

        val result = useCase(inputMissingHeading, weatherMissingDirection)

        assertNull(result.pointOfSailAngleDegrees)
        assertNull(result.pointOfSail)
        assertNull(result.apparentWind)
        assertNull(result.apparentWindAngleDegrees)
    }

    @Test
    fun `normalizes wrap around headings for exact angle`() {
        val input = TelemetryReading(speedMps = 6.0, headingDegrees = 350.0)
        val weather = weatherSnapshot(windSpeedKts = 14.0, windDirectionDegrees = 10)

        val result = useCase(input, weather)

        assertEquals(20.0, result.pointOfSailAngleDegrees ?: -1.0, 0.0001)
        assertEquals(PointOfSail.IN_IRONS, result.pointOfSail)
    }

    private fun weatherSnapshot(
        windSpeedKts: Double?,
        windDirectionDegrees: Int?,
    ): WeatherSnapshot {
        return WeatherSnapshot(
            location = WeatherLocation(
                latitude = 52.52,
                longitude = 13.41,
                timezone = "Europe/Berlin",
                cellSelection = WeatherCellSelection.NEAREST,
            ),
            observedAt = Instant.parse("2026-01-01T00:00:00Z"),
            validFrom = Instant.parse("2026-01-01T00:00:00Z"),
            validUntil = Instant.parse("2026-01-01T00:15:00Z"),
            sourceType = WeatherSourceType.FORECAST,
            source = "test",
            windSpeedKts = windSpeedKts,
            windDirectionDegrees = windDirectionDegrees,
        )
    }
}
