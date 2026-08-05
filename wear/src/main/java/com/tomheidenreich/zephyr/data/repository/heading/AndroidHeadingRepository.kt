package com.tomheidenreich.zephyr.data.repository.heading

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.repository.HeadingRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidHeadingRepository(
    context: Context,
) : HeadingRepository {
    private val appContext = context.applicationContext
    private val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val headingSmoother = CircularHeadingSmoother()

    override fun observeHeading(): Flow<RepositoryResult<Double>> = callbackFlow {
        trySend(RepositoryResult.Loading)

        val locationListener = registerLocationBasedHeading(this)
        if (locationListener != null) {
            awaitClose {
                locationManager.removeUpdates(locationListener)
            }
            return@callbackFlow
        }

        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

        if (rotationSensor != null) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val headingDegrees = headingFromRotationVector(event.values) ?: return
                    headingSmoother.update(headingDegrees)?.let { smoothedHeadingDegrees ->
                        trySend(RepositoryResult.Data(smoothedHeadingDegrees))
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            awaitClose {
                sensorManager.unregisterListener(listener)
            }
            return@callbackFlow
        }

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (accelerometer != null && magnetometer != null) {
            val gravity = FloatArray(3)
            val geomagnetic = FloatArray(3)
            var gravityReady = false
            var geomagneticReady = false

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                            copySensorValues(event.values, gravity)
                            gravityReady = true
                        }

                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            copySensorValues(event.values, geomagnetic)
                            geomagneticReady = true
                        }
                    }

                    if (!gravityReady || !geomagneticReady) return

                    val rotationMatrix = FloatArray(9)
                    val inclinationMatrix = FloatArray(9)
                    if (!SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)) {
                        return
                    }

                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val azimuthDegrees = Math.toDegrees(orientation[0].toDouble())
                    headingSmoother.update(normalizeDegrees(azimuthDegrees))?.let { smoothedHeadingDegrees ->
                        trySend(RepositoryResult.Data(smoothedHeadingDegrees))
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
            awaitClose {
                sensorManager.unregisterListener(listener)
            }
            return@callbackFlow
        }

        trySend(RepositoryResult.Error(IllegalStateException("No supported heading sensors available")))
        close()
    }

    override suspend fun clearHeading() = Unit

    private fun registerLocationBasedHeading(trySendScope: kotlinx.coroutines.channels.ProducerScope<RepositoryResult<Double>>): LocationListener? {
        if (!hasLocationPermission()) return null

        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { provider -> runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false) }
        if (providers.isEmpty()) return null

        var previousSample: HeadingSample? = seedHeadingSample(providers)
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val currentSample = location.toHeadingSample()
                val headingDegrees = CourseHeadingCalculator.resolveHeading(previousSample, currentSample)
                previousSample = currentSample
                if (headingDegrees != null) {
                    headingSmoother.update(headingDegrees)?.let { smoothedHeadingDegrees ->
                        trySendScope.trySend(RepositoryResult.Data(smoothedHeadingDegrees))
                    }
                }
            }
        }

        providers.forEach { provider ->
            runCatching {
                locationManager.requestLocationUpdates(provider, 1000L, 0f, listener, Looper.getMainLooper())
            }
        }
        return listener
    }

    private fun seedHeadingSample(providers: List<String>): HeadingSample? {
        val lastKnownLocations = providers.mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }

        val latest = lastKnownLocations.maxByOrNull { it.time } ?: return null
        return latest.toHeadingSample()
    }

    private fun Location.toHeadingSample(): HeadingSample {
        return HeadingSample(
            latitude = latitude,
            longitude = longitude,
            speedMps = if (hasSpeed()) speed.toDouble() else null,
            bearingDegrees = if (hasBearing()) bearing.toDouble() else null,
        )
    }

    private fun hasLocationPermission(): Boolean {
        val coarseGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val fineGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return coarseGranted || fineGranted
    }

    private fun headingFromRotationVector(values: FloatArray): Double? {
        val rotationMatrix = FloatArray(9)
        when (values.size) {
            3, 4, 5 -> SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
            else -> return null
        }

        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)
        return normalizeDegrees(Math.toDegrees(orientation[0].toDouble()))
    }

    private fun copySensorValues(source: FloatArray, target: FloatArray) {
        for (index in target.indices) {
            target[index] = source.getOrNull(index) ?: 0f
        }
    }

    private fun normalizeDegrees(degrees: Double): Double {
        val mod = degrees % 360.0
        return if (mod < 0) mod + 360.0 else mod
    }
}