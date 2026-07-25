package com.tomheidenreich.zephyr.domain.surface

import java.time.Instant

data class AppSurfaceSnapshot(
    val generatedAt: Instant,
    val headline: String,
    val subline: String,
    val isStale: Boolean,
)