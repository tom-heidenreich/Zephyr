package com.tomheidenreich.zephyr.domain.model

enum class WindReference {
    TRUE,
    APPARENT,
}

enum class WindDataSourceType {
    API,
    INSTRUMENT,
    FUSED,
}

data class WindSample(
    val speedKts: Double,
    val directionFromDegrees: Int? = null,
    val reference: WindReference,
)
