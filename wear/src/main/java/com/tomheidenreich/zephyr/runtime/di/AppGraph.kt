package com.tomheidenreich.zephyr.runtime.di

import com.tomheidenreich.zephyr.data.repository.fake.FakeExerciseSessionRepository
import com.tomheidenreich.zephyr.data.repository.fake.FakeLiveMetricsRepository
import com.tomheidenreich.zephyr.data.repository.fake.FakeSurfaceSnapshotRepository
import com.tomheidenreich.zephyr.data.repository.fake.FakeUserPreferencesRepository
import com.tomheidenreich.zephyr.data.repository.fake.FakeWindRepository
import com.tomheidenreich.zephyr.domain.repository.ExerciseSessionRepository
import com.tomheidenreich.zephyr.domain.repository.LiveMetricsRepository
import com.tomheidenreich.zephyr.domain.repository.SurfaceSnapshotRepository
import com.tomheidenreich.zephyr.domain.repository.UserPreferencesRepository
import com.tomheidenreich.zephyr.domain.repository.WindRepository

/**
 * Foundation DI graph. Repositories are fake for now and can be swapped with real implementations.
 */
object AppGraph {
    val exerciseSessionRepository: ExerciseSessionRepository by lazy { FakeExerciseSessionRepository() }
    val liveMetricsRepository: LiveMetricsRepository by lazy { FakeLiveMetricsRepository() }
    val windRepository: WindRepository by lazy { FakeWindRepository() }
    val surfaceSnapshotRepository: SurfaceSnapshotRepository by lazy { FakeSurfaceSnapshotRepository() }
    val userPreferencesRepository: UserPreferencesRepository by lazy { FakeUserPreferencesRepository() }
}
