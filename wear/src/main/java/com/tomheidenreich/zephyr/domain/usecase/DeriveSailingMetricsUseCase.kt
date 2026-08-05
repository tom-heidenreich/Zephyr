package com.tomheidenreich.zephyr.domain.usecase

import com.tomheidenreich.zephyr.domain.model.SailingMetrics
import com.tomheidenreich.zephyr.domain.sailing.PointOfSail
import com.tomheidenreich.zephyr.domain.model.TelemetryReading
import com.tomheidenreich.zephyr.domain.wind.WindReference
import com.tomheidenreich.zephyr.domain.wind.WindSample
import com.tomheidenreich.zephyr.domain.weather.WeatherSnapshot
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val MPS_TO_KNOTS = 1.9438444924406

/**
 * Computes sailing-specific derived values:
 * 1) True wind when only apparent wind + vessel motion are known.
 * 2) Point of sail from vessel heading relative to true wind direction.
 * 3) Velocity made good from vessel velocity and heading.
 */
class DeriveSailingMetricsUseCase {
    operator fun invoke(
        telemetry: TelemetryReading,
        weather: WeatherSnapshot?,
    ): SailingMetrics {
        val trueWind = weather?.windSpeedKts?.let { speedKts ->
            WindSample(
                speedKts = speedKts,
                directionFromDegrees = weather.windDirectionDegrees,
                reference = WindReference.TRUE,
            )
        }

        val apparentWind = calculateApparentWind(
            telemetry = telemetry,
            trueWind = trueWind,
        )
        val apparentWindAngle = apparentWind?.directionFromDegrees?.let { apparentWindFromDegrees ->
            calculatePointOfSailAngle(
                headingDegrees = telemetry.headingDegrees,
                trueWindFromDegrees = apparentWindFromDegrees,
            )
        }

        val pointOfSailAngle = calculatePointOfSailAngle(
            headingDegrees = telemetry.headingDegrees,
            trueWindFromDegrees = trueWind?.directionFromDegrees,
        )
        val pointOfSail = pointOfSailAngle?.let(::classifyPointOfSail)

        val velocityMadeGoodMps = calculateVelocityMadeGood(
            vesselVelocity = telemetry.speedMps,
            headingDegrees = telemetry.headingDegrees,
            trueWindFromDirectionDegrees = trueWind?.directionFromDegrees,
        )
        val velocityMadeGoodEfficiency = calculateVelocityMadeGoodEfficiency(
            velocityMadeGoodMps = velocityMadeGoodMps,
            trueWindSpeedKts = trueWind?.speedKts,
        )

        return SailingMetrics(
            trueWind = trueWind,
            apparentWind = apparentWind,
            apparentWindAngleDegrees = apparentWindAngle,
            pointOfSailAngleDegrees = pointOfSailAngle,
            pointOfSail = pointOfSail,
            velocityMadeGoodMps = velocityMadeGoodMps,
            velocityMadeGoodEfficiency = velocityMadeGoodEfficiency,
        )
    }

    private fun calculateApparentWind(
        telemetry: TelemetryReading,
        trueWind: WindSample?,
    ): WindSample? {
        val trueWindSpeed = trueWind?.speedKts ?: return null
        val trueWindFromDegrees = trueWind.directionFromDegrees ?: return null
        val headingDegrees = telemetry.headingDegrees ?: return null
        val vesselSpeedMps = telemetry.speedMps ?: return null

        val vesselSpeedKts = vesselSpeedMps * MPS_TO_KNOTS
        val trueAirMovement = vectorFromHeading(
            headingDegrees = (trueWindFromDegrees + 180.0).normalizeDegrees(),
            speed = trueWindSpeed,
        )
        val vesselMovement = vectorFromHeading(
            headingDegrees = headingDegrees.normalizeDegrees(),
            speed = vesselSpeedKts,
        )

        val apparentAirMovement = Vector2(
            x = trueAirMovement.x - vesselMovement.x,
            y = trueAirMovement.y - vesselMovement.y,
        )
        val apparentSpeedKts = apparentAirMovement.magnitude()
        if (apparentSpeedKts <= 0.0) return null

        val apparentFromDegrees = (vectorToHeading(apparentAirMovement) + 180.0).normalizeDegrees().roundToInt()
        return WindSample(
            speedKts = apparentSpeedKts,
            directionFromDegrees = apparentFromDegrees,
            reference = WindReference.APPARENT,
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
        trueWindFromDirectionDegrees: Int?,
    ): Double? {
        if (vesselVelocity == null) return null
        if (headingDegrees == null) return null
        if (trueWindFromDirectionDegrees == null) return null

        val relativeHeading =
            kotlin.math.abs(headingDegrees - trueWindFromDirectionDegrees.toDouble())

        return vesselVelocity * kotlin.math.abs(cos(relativeHeading.normalizeDegrees().toRadians()))
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
        val windSpeedKts = trueWindSpeedKts ?: return null
        if (windSpeedKts <= 0.0) return null

        return velocityMadeGoodKts / windSpeedKts
    }

    private fun angularDistance(a: Double, b: Double): Double {
        val diff = abs(a - b)
        return minOf(diff, 360.0 - diff)
    }

    private fun vectorFromHeading(headingDegrees: Double, speed: Double): Vector2 {
        val radians = headingDegrees.toRadians()
        return Vector2(
            x = speed * sin(radians),
            y = speed * cos(radians),
        )
    }

    private fun vectorToHeading(vector: Vector2): Double {
        return Math.toDegrees(atan2(vector.x, vector.y)).normalizeDegrees()
    }

    private fun Double.normalizeDegrees(): Double {
        val mod = this % 360.0
        return if (mod < 0) mod + 360.0 else mod
    }

    private fun Double.toRadians(): Double = Math.toRadians(this)

    private fun Double.toDegrees(): Double = Math.toDegrees(this)

    private data class Vector2(
        val x: Double,
        val y: Double,
    ) {
        fun magnitude(): Double = kotlin.math.sqrt((x * x) + (y * y))
    }
}
