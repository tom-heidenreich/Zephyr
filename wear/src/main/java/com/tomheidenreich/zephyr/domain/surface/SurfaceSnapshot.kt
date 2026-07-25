package com.tomheidenreich.zephyr.domain.surface

import java.time.Instant

data class SurfaceSnapshot(
    val generatedAt: Instant,
    val headline: String,
    val subline: String,
    val complicationShortText: String,
    val complicationContentDescription: String,
    val isStale: Boolean,
)