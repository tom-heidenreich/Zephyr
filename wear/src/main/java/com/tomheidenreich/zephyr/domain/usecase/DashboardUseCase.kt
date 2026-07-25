package com.tomheidenreich.zephyr.domain.usecase

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.model.LiveMetrics
import com.tomheidenreich.zephyr.domain.model.SessionState
import com.tomheidenreich.zephyr.domain.model.SurfaceSnapshot
import com.tomheidenreich.zephyr.domain.model.WindObservation
import com.tomheidenreich.zephyr.domain.repository.ExerciseSessionRepository
import com.tomheidenreich.zephyr.domain.repository.LiveMetricsRepository
import com.tomheidenreich.zephyr.domain.repository.SurfaceSnapshotRepository
import com.tomheidenreich.zephyr.domain.repository.WindRepository
import kotlinx.coroutines.flow.Flow

/**
 * Presentation-facing facade that keeps UI code away from repositories.
 */
class DashboardUseCase(
    private val exerciseSessionRepository: ExerciseSessionRepository,
    private val liveMetricsRepository: LiveMetricsRepository,
    private val windRepository: WindRepository,
    private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
) {
    fun observeSessionState(): Flow<RepositoryResult<SessionState>> {
        return exerciseSessionRepository.observeSessionState()
    }

    fun observeLiveMetrics(): Flow<RepositoryResult<LiveMetrics>> {
        return liveMetricsRepository.observeLiveMetrics()
    }

    fun observeCurrentWind(spotId: String): Flow<RepositoryResult<WindObservation>> {
        return windRepository.observeCurrentWind(spotId)
    }

    fun observeLatestSnapshot(): Flow<SurfaceSnapshot?> {
        return surfaceSnapshotRepository.observeLatestSnapshot()
    }

    suspend fun startSession(sessionName: String? = null): RepositoryResult<SessionState> {
        return exerciseSessionRepository.startSession(sessionName)
    }

    suspend fun pauseSession(): RepositoryResult<SessionState> {
        return exerciseSessionRepository.pauseSession()
    }

    suspend fun resumeSession(): RepositoryResult<SessionState> {
        return exerciseSessionRepository.resumeSession()
    }

    suspend fun endSession(): RepositoryResult<SessionState> {
        return exerciseSessionRepository.endSession()
    }
}