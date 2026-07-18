package com.tomheidenreich.zephyr.domain.model

import java.time.Instant

data class WindForecast(
    val spotId: String,
    val generatedAt: Instant,
    val entries: List<WindForecastEntry>,
    val source: String,
)

data class WindForecastEntry(
    val at: Instant,
    val speedKts: Double,
    val gustKts: Double? = null,
    val directionDegrees: Int? = null,
)
