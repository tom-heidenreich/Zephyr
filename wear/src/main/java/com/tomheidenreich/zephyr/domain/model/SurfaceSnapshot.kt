package com.tomheidenreich.zephyr.domain.model

import java.time.Instant

data class SurfaceSnapshot(
    val generatedAt: Instant,
    val headline: String,
    val subline: String,
    val complicationShortText: String,
    val complicationContentDescription: String,
    val isStale: Boolean,
)
