package com.tomheidenreich.zephyr.runtime.di

import android.content.Context
import com.tomheidenreich.zephyr.data.repository.fake.FakeExerciseSessionRepository
import com.tomheidenreich.zephyr.data.repository.fake.FakeLiveMetricsRepository
import com.tomheidenreich.zephyr.data.repository.fake.FakeSurfaceSnapshotRepository
import com.tomheidenreich.zephyr.data.repository.fake.FakeUserPreferencesRepository
import com.tomheidenreich.zephyr.data.repository.fake.FakeWindRepository
import com.tomheidenreich.zephyr.data.repository.health.HealthServicesExerciseSessionRepository
import com.tomheidenreich.zephyr.data.repository.health.HealthServicesLiveMetricsRepository
import com.tomheidenreich.zephyr.data.source.health.HealthServicesExerciseBridge
import com.tomheidenreich.zephyr.domain.repository.ExerciseSessionRepository
import com.tomheidenreich.zephyr.domain.repository.LiveMetricsRepository
import com.tomheidenreich.zephyr.domain.repository.SurfaceSnapshotRepository
import com.tomheidenreich.zephyr.domain.repository.UserPreferencesRepository
import com.tomheidenreich.zephyr.domain.repository.WindRepository

/**
 * Foundation DI graph. Fake repositories stay available for previews until the app initializes.
 */
object AppGraph {
    private val fakeExerciseSessionRepository = FakeExerciseSessionRepository()
    private val fakeLiveMetricsRepository = FakeLiveMetricsRepository()
    private val fakeWindRepository = FakeWindRepository()
    private val fakeSurfaceSnapshotRepository = FakeSurfaceSnapshotRepository()
    private val fakeUserPreferencesRepository = FakeUserPreferencesRepository()

    @Volatile
    private var healthServicesContext: Context? = null

    private val healthServicesBridge: HealthServicesExerciseBridge by lazy {
        HealthServicesExerciseBridge(requireNotNull(healthServicesContext))
    }

    private val healthServicesExerciseSessionRepository: ExerciseSessionRepository by lazy {
        HealthServicesExerciseSessionRepository(healthServicesBridge)
    }

    private val healthServicesLiveMetricsRepository: LiveMetricsRepository by lazy {
        HealthServicesLiveMetricsRepository(healthServicesBridge)
    }

    fun initialize(context: Context) {
        healthServicesContext = context.applicationContext
    }

    val exerciseSessionRepository: ExerciseSessionRepository
        get() = if (healthServicesContext == null) fakeExerciseSessionRepository else healthServicesExerciseSessionRepository

    val liveMetricsRepository: LiveMetricsRepository
        get() = if (healthServicesContext == null) fakeLiveMetricsRepository else healthServicesLiveMetricsRepository

    val windRepository: WindRepository = fakeWindRepository
    val surfaceSnapshotRepository: SurfaceSnapshotRepository = fakeSurfaceSnapshotRepository
    val userPreferencesRepository: UserPreferencesRepository = fakeUserPreferencesRepository
}
