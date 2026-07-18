package com.tomheidenreich.zephyr.domain.model

import java.time.Instant

data class LiveMetrics(
    val timestamp: Instant = Instant.now(),
    val speedMps: Double? = null,
    val trueWind: WindSample? = null,
    val apparentWind: WindSample? = null,
    val headingDegrees: Double? = null,
    val pointOfSailAngleDegrees: Double? = null,
    val pointOfSail: PointOfSail? = null,
    val heartRateBpm: Double? = null,
    val distanceMeters: Double = 0.0,
)
