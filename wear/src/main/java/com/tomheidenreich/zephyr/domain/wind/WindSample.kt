package com.tomheidenreich.zephyr.domain.wind

data class WindSample(
    val speedKts: Double,
    val directionFromDegrees: Int? = null,
    val reference: WindReference,
)