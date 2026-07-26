package com.tomheidenreich.zephyr.domain.model

import com.tomheidenreich.zephyr.domain.sailing.PointOfSail
import com.tomheidenreich.zephyr.domain.wind.WindSample

data class SailingMetrics(
    val trueWind: WindSample? = null,
    val apparentWind: WindSample? = null,
    val pointOfSailAngleDegrees: Double? = null,
    val pointOfSail: PointOfSail? = null,
    val velocityMadeGoodMps: Double? = null,
    val velocityMadeGoodEfficiency: Double? = null,
)