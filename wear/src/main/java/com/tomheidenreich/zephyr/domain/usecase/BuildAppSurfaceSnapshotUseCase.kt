package com.tomheidenreich.zephyr.domain.usecase

import com.tomheidenreich.zephyr.domain.model.SailingMetrics
import com.tomheidenreich.zephyr.domain.session.SessionState
import com.tomheidenreich.zephyr.domain.surface.AppSurfaceSnapshot
import com.tomheidenreich.zephyr.domain.model.TelemetryReading
import com.tomheidenreich.zephyr.domain.wind.WindObservation
import java.time.Instant

/**
 * Builds an app-tailored snapshot for the main watch screen.
 */
class BuildAppSurfaceSnapshotUseCase {
    operator fun invoke(
        session: SessionState,
        telemetry: TelemetryReading?,
        sailingMetrics: SailingMetrics?,
        wind: WindObservation?,
    ): AppSurfaceSnapshot {
        val sessionText = session.status.name
        val speedText = telemetry?.speedMps?.let { "${"%.1f".format(it)} m/s" } ?: "-- m/s"
        val pointOfSailText = sailingMetrics?.pointOfSail?.name?.replace('_', ' ') ?: "UNKNOWN"
        val windText = sailingMetrics?.trueWind?.speedKts?.let { "${"%.0f".format(it)} kt TW" }
            ?: wind?.trueWind?.speedKts?.let { "${"%.0f".format(it)} kt TW" }
            ?: "-- kt TW"

        return AppSurfaceSnapshot(
            generatedAt = Instant.now(),
            headline = "$speedText | $windText",
            subline = "$pointOfSailText | $sessionText",
            isStale = false,
        )
    }
}
