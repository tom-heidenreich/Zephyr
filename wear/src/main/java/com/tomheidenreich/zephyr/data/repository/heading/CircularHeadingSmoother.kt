package com.tomheidenreich.zephyr.data.repository.heading

internal class CircularHeadingSmoother(
    private val minimumDeltaDegrees: Double = 3.0,
    private val smoothingFactor: Double = 0.2,
) {
    private var lastHeadingDegrees: Double? = null

    fun update(headingDegrees: Double?): Double? {
        val incomingHeading = headingDegrees?.normalizeAngleValue() ?: return lastHeadingDegrees
        val previousHeading = lastHeadingDegrees ?: run {
            lastHeadingDegrees = incomingHeading
            return incomingHeading
        }

        val delta = shortestSignedDelta(previousHeading, incomingHeading)
        if (kotlin.math.abs(delta) < minimumDeltaDegrees) {
            return previousHeading
        }

        val smoothedHeading = normalizeAngleDegrees(previousHeading + (delta * smoothingFactor))
        lastHeadingDegrees = smoothedHeading
        return smoothedHeading
    }

    private fun shortestSignedDelta(fromDegrees: Double, toDegrees: Double): Double {
        val delta = normalizeAngleDegrees(toDegrees - fromDegrees)
        return if (delta > 180.0) delta - 360.0 else delta
    }

    private fun Double.normalizeAngleValue(): Double {
        val mod = this % 360.0
        return if (mod < 0) mod + 360.0 else mod
    }

    private fun normalizeAngleDegrees(degrees: Double): Double {
        val mod = degrees % 360.0
        return if (mod < 0) mod + 360.0 else mod
    }
}