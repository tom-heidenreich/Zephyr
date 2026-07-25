package com.tomheidenreich.zephyr.domain.model

import java.time.Instant

data class TelemetryReading(
    val timestamp: Instant = Instant.now(),
    val speedMps: Double? = null,
    val headingDegrees: Double? = null,
    val heartRateBpm: Double? = null,
    val distanceMeters: Double = 0.0,
)
