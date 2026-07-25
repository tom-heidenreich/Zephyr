package com.tomheidenreich.zephyr.domain.wind

import java.time.Instant

data class WindObservation(
    val spotId: String,
    val observedAt: Instant,
    val trueWind: WindSample? = null,
    val apparentWind: WindSample? = null,
    val sourceType: WindDataSourceType,
    val source: String,
)