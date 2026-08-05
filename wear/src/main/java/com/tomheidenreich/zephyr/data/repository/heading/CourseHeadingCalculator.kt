package com.tomheidenreich.zephyr.data.repository.heading

internal data class HeadingSample(
    val latitude: Double,
    val longitude: Double,
    val speedMps: Double? = null,
    val bearingDegrees: Double? = null,
)

internal object CourseHeadingCalculator {
    private const val MIN_MOVING_SPEED_MPS = 0.5
    private const val MIN_DISTANCE_METERS = 2.0

    fun resolveHeading(
        previous: HeadingSample?,
        current: HeadingSample,
    ): Double? {
        current.bearingDegrees?.let { bearingDegrees ->
            if ((current.speedMps ?: 0.0) >= MIN_MOVING_SPEED_MPS) {
                return normalizeDegrees(bearingDegrees)
            }
        }

        previous ?: return null
        if (distanceMeters(previous, current) < MIN_DISTANCE_METERS) return null

        return initialBearingDegrees(previous, current)
    }

    private fun initialBearingDegrees(previous: HeadingSample, current: HeadingSample): Double {
        val previousLatitude = previous.latitude.toRadians()
        val currentLatitude = current.latitude.toRadians()
        val deltaLongitude = (current.longitude - previous.longitude).toRadians()

        val y = kotlin.math.sin(deltaLongitude) * kotlin.math.cos(currentLatitude)
        val x = kotlin.math.cos(previousLatitude) * kotlin.math.sin(currentLatitude) -
                kotlin.math.sin(previousLatitude) * kotlin.math.cos(currentLatitude) * kotlin.math.cos(deltaLongitude)

        return normalizeDegrees(Math.toDegrees(kotlin.math.atan2(y, x)))
    }

    private fun distanceMeters(previous: HeadingSample, current: HeadingSample): Double {
        val earthRadiusMeters = 6_371_000.0
        val previousLatitude = previous.latitude.toRadians()
        val currentLatitude = current.latitude.toRadians()
        val deltaLatitude = (current.latitude - previous.latitude).toRadians()
        val deltaLongitude = (current.longitude - previous.longitude).toRadians()

        val a = kotlin.math.sin(deltaLatitude / 2).let { it * it } +
                kotlin.math.cos(previousLatitude) * kotlin.math.cos(currentLatitude) *
                kotlin.math.sin(deltaLongitude / 2).let { it * it }
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadiusMeters * c
    }

    private fun Double.toRadians(): Double = Math.toRadians(this)

    private fun normalizeDegrees(degrees: Double): Double {
        val mod = degrees % 360.0
        return if (mod < 0) mod + 360.0 else mod
    }
}