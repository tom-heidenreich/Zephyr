package com.tomheidenreich.zephyr.domain.usecase

import com.tomheidenreich.zephyr.domain.model.SailingMetrics
import com.tomheidenreich.zephyr.domain.sailing.PointOfSail
import com.tomheidenreich.zephyr.domain.model.TelemetryReading
import com.tomheidenreich.zephyr.domain.wind.WindObservation
import com.tomheidenreich.zephyr.domain.wind.WindReference
import com.tomheidenreich.zephyr.domain.wind.WindSample
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Computes sailing-specific derived values:
 * 1) True wind when only apparent wind + vessel motion are known.
 * 2) Point of sail from vessel heading relative to true wind direction.
 * 3) Velocity made good from vessel velocity and heading.
 */
class DeriveSailingMetricsUseCase {
    operator fun invoke(
        telemetry: TelemetryReading,
        windObservation: WindObservation?,
    ): SailingMetrics {
        val observedTrueWind = windObservation?.trueWind
        val observedApparentWind = windObservation?.apparentWind

        val trueWind = observedTrueWind
            ?: calculateTrueWindFromApparent(
                apparentWind = observedApparentWind,
                speedMps = telemetry.speedMps,
                headingDegrees = telemetry.headingDegrees,
            )

        val pointOfSailAngle = calculatePointOfSailAngle(
            headingDegrees = telemetry.headingDegrees,
            trueWindFromDegrees = trueWind?.directionFromDegrees,
        )
        val pointOfSail = pointOfSailAngle?.let(::classifyPointOfSail)

        val velocityMadeGoodMps = calculateVelocityMadeGood(
            vesselVelocity = telemetry.speedMps,
            headingDegrees = telemetry.headingDegrees,
        )
        val velocityMadeGoodEfficiency = calculateVelocityMadeGoodEfficiency(
            velocityMadeGoodMps = velocityMadeGoodMps,
            trueWindSpeedKts = trueWind?.speedKts,
        )

        return SailingMetrics(
            trueWind = trueWind,
            apparentWind = observedApparentWind,
            pointOfSailAngleDegrees = pointOfSailAngle,
            pointOfSail = pointOfSail,
            velocityMadeGoodMps = velocityMadeGoodMps,
            velocityMadeGoodEfficiency = velocityMadeGoodEfficiency
        )
    }

    private fun calculateTrueWindFromApparent(
        apparentWind: WindSample?,
        speedMps: Double?,
        headingDegrees: Double?,
    ): WindSample? {
        val apparent = apparentWind ?: return null
        val heading = headingDegrees ?: return null
        val directionFrom = apparent.directionFromDegrees ?: return null
        val boatSpeedKts = speedMps?.times(MPS_TO_KNOTS) ?: return null

        // Meteorological direction is "from"; vector math is easier in "toward" coordinates.
        val apparentToward = (directionFrom + 180.0).normalizeDegrees()
        val apparentEast = apparent.speedKts * sin(apparentToward.toRadians())
        val apparentNorth = apparent.speedKts * cos(apparentToward.toRadians())

        val headingToward = heading.normalizeDegrees()
        val boatEast = boatSpeedKts * sin(headingToward.toRadians())
        val boatNorth = boatSpeedKts * cos(headingToward.toRadians())

        // Apparent wind equals true wind minus vessel velocity in ground frame.
        val trueEast = apparentEast + boatEast
        val trueNorth = apparentNorth + boatNorth

        val trueSpeed = kotlin.math.sqrt((trueEast * trueEast) + (trueNorth * trueNorth))
        val trueToward = atan2(trueEast, trueNorth).toDegrees().normalizeDegrees()
        val trueFrom = (trueToward + 180.0).normalizeDegrees().roundToInt()

        return WindSample(
            speedKts = trueSpeed,
            directionFromDegrees = trueFrom,
            reference = WindReference.TRUE,
        )
    }

    private fun calculatePointOfSailAngle(
        headingDegrees: Double?,
        trueWindFromDegrees: Int?,
    ): Double? {
        val heading = headingDegrees ?: return null
        val windFrom = trueWindFromDegrees ?: return null

        return angularDistance(heading.normalizeDegrees(), windFrom.toDouble().normalizeDegrees())
    }

    private fun classifyPointOfSail(delta: Double): PointOfSail {
        return when {
            delta < 35.0 -> PointOfSail.IN_IRONS
            delta < 60.0 -> PointOfSail.CLOSE_HAULED
            delta < 80.0 -> PointOfSail.CLOSE_REACH
            delta < 120.0 -> PointOfSail.BEAM_REACH
            delta < 160.0 -> PointOfSail.BROAD_REACH
            else -> PointOfSail.RUNNING
        }
    }

    private fun calculateVelocityMadeGood(
        vesselVelocity: Double?,
        headingDegrees: Double?,
    ): Double? {
        if (vesselVelocity == null) return null
        if (headingDegrees == null) return null

        return vesselVelocity * cos(headingDegrees.toRadians())
    }

    /**
     * You would use a target VMG derived by maximum speed the vessel can achieve by design and wind conditions.
     * Because this is hard to obtain, the efficieny is simplified, but with the cost of efficiency values 
     * not really being comparable in different wind speeds.
     */
    private fun calculateVelocityMadeGoodEfficiency(
        velocityMadeGoodMps: Double?,
        trueWindSpeedKts: Double?,
    ): Double? {
        val velocityMadeGoodKts = velocityMadeGoodMps?.times(MPS_TO_KNOTS) ?: return null
        if (trueWindSpeedKts == null) return null

        return velocityMadeGoodKts / trueWindSpeedKts
    }

    private fun angularDistance(a: Double, b: Double): Double {
        val diff = abs(a - b)
        return minOf(diff, 360.0 - diff)
    }

    private fun Double.normalizeDegrees(): Double {
        val mod = this % 360.0
        return if (mod < 0) mod + 360.0 else mod
    }

    private fun Double.toRadians(): Double = Math.toRadians(this)

    private fun Double.toDegrees(): Double = Math.toDegrees(this)

    companion object {
        private const val MPS_TO_KNOTS = 1.943844492
    }
}
