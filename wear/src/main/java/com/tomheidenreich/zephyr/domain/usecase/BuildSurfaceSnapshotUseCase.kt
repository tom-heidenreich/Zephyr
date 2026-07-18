package com.tomheidenreich.zephyr.domain.usecase

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.model.LiveMetrics
import com.tomheidenreich.zephyr.domain.model.SessionState
import com.tomheidenreich.zephyr.domain.model.SurfaceSnapshot
import com.tomheidenreich.zephyr.domain.model.WindObservation
import java.time.Instant

/**
 * Builds a compact snapshot suitable for app summary, tile, and complication surfaces.
 */
class BuildSurfaceSnapshotUseCase {
    operator fun invoke(
        session: RepositoryResult<SessionState>,
        metrics: RepositoryResult<LiveMetrics>,
        wind: RepositoryResult<WindObservation>,
    ): SurfaceSnapshot {
        val sessionText = (session as? RepositoryResult.Data)?.value?.status?.name ?: "NO SESSION"
        val metricValue = (metrics as? RepositoryResult.Data)?.value
        val speedText = metricValue?.speedMps?.let { "${"%.1f".format(it)} m/s" } ?: "-- m/s"
        val pointOfSailText = metricValue?.pointOfSail?.name?.replace('_', ' ') ?: "UNKNOWN"
        val windText = metricValue?.trueWind?.speedKts?.let { "${"%.0f".format(it)} kt TW" }
            ?: (wind as? RepositoryResult.Data)?.value?.trueWind?.speedKts?.let { "${"%.0f".format(it)} kt TW" }
            ?: "-- kt TW"

        return SurfaceSnapshot(
            generatedAt = Instant.now(),
            headline = "$speedText | $windText",
            subline = "$pointOfSailText | $sessionText",
            complicationShortText = pointOfSailText,
            complicationContentDescription = "True wind $windText, point of sail $pointOfSailText, session $sessionText",
            isStale = false,
        )
    }
}
