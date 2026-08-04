package com.tomheidenreich.zephyr.runtime.di

import android.content.Context
import com.tomheidenreich.zephyr.data.repository.fake.FakeExerciseSessionRepository
import com.tomheidenreich.zephyr.data.repository.fake.FakeTelemetryRepository
import com.tomheidenreich.zephyr.data.repository.fake.FakeSurfaceSnapshotRepository
import com.tomheidenreich.zephyr.data.repository.fake.FakeUserPreferencesRepository
import com.tomheidenreich.zephyr.data.repository.fake.FakeWeatherRepository
import com.tomheidenreich.zephyr.data.repository.weather.OpenMeteoWeatherRepository
import com.tomheidenreich.zephyr.data.repository.health.HealthServicesExerciseSessionRepository
import com.tomheidenreich.zephyr.data.repository.health.HealthServicesTelemetryRepository
import com.tomheidenreich.zephyr.data.source.health.HealthServicesExerciseBridge
import com.tomheidenreich.zephyr.data.source.weather.AndroidCurrentPositionWeatherLocationProvider
import com.tomheidenreich.zephyr.data.source.weather.HttpOpenMeteoClient
import com.tomheidenreich.zephyr.domain.repository.ExerciseSessionRepository
import com.tomheidenreich.zephyr.domain.repository.TelemetryRepository
import com.tomheidenreich.zephyr.domain.repository.SurfaceSnapshotRepository
import com.tomheidenreich.zephyr.domain.repository.UserPreferencesRepository
import com.tomheidenreich.zephyr.domain.repository.WeatherRepository

/**
 * Foundation DI graph. Fake repositories stay available for previews until the app initializes.
 */
object AppGraph {
    private val fakeExerciseSessionRepository = FakeExerciseSessionRepository()
    private val fakeTelemetryRepository = FakeTelemetryRepository()
    private val fakeWeatherRepository = FakeWeatherRepository()
    private val fakeSurfaceSnapshotRepository = FakeSurfaceSnapshotRepository()
    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository()

    @Volatile
    private var healthServicesContext: Context? = null

    @Volatile
    private var applicationContext: Context? = null

    private val healthServicesBridge: HealthServicesExerciseBridge by lazy {
        HealthServicesExerciseBridge(requireNotNull(healthServicesContext))
    }

    private val healthServicesExerciseSessionRepository: ExerciseSessionRepository by lazy {
        HealthServicesExerciseSessionRepository(healthServicesBridge)
    }

    private val healthServicesTelemetryRepository: TelemetryRepository by lazy {
        HealthServicesTelemetryRepository(healthServicesBridge)
    }

    private val openMeteoWeatherRepository: WeatherRepository by lazy {
        val context = requireNotNull(applicationContext)
        OpenMeteoWeatherRepository(
            locationProvider = AndroidCurrentPositionWeatherLocationProvider(context),
            client = HttpOpenMeteoClient(),
        )
    }

    fun initialize(context: Context) {
        val appContext = context.applicationContext
        applicationContext = appContext
        healthServicesContext = appContext
    }

    val exerciseSessionRepository: ExerciseSessionRepository
        get() = if (healthServicesContext == null) fakeExerciseSessionRepository else healthServicesExerciseSessionRepository

    val telemetryRepository: TelemetryRepository
        get() = if (healthServicesContext == null) fakeTelemetryRepository else healthServicesTelemetryRepository

    val weatherRepository: WeatherRepository
        get() = applicationContext?.let { openMeteoWeatherRepository } ?: fakeWeatherRepository
    val surfaceSnapshotRepository: SurfaceSnapshotRepository = fakeSurfaceSnapshotRepository
    val userPreferencesRepository: UserPreferencesRepository = fakeUserPreferencesRepository
}
