package com.tomheidenreich.zephyr.data.repository.health

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.data.source.health.HealthServicesExerciseBridge
import com.tomheidenreich.zephyr.domain.model.SessionState
import com.tomheidenreich.zephyr.domain.repository.ExerciseSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class HealthServicesExerciseSessionRepository(
    private val bridge: HealthServicesExerciseBridge,
) : ExerciseSessionRepository {
    override fun observeSessionState(): Flow<RepositoryResult<SessionState>> {
        return bridge.observeSessionState()
            .map<SessionState, RepositoryResult<SessionState>> { RepositoryResult.Data(it) }
            .catch { emit(RepositoryResult.Error(it)) }
    }

    override suspend fun startSession(sessionName: String?): RepositoryResult<SessionState> {
        return RepositoryResult.Data(bridge.startSession(sessionName))
    }

    override suspend fun pauseSession(): RepositoryResult<SessionState> {
        return RepositoryResult.Data(bridge.pauseSession())
    }

    override suspend fun resumeSession(): RepositoryResult<SessionState> {
        return RepositoryResult.Data(bridge.resumeSession())
    }

    override suspend fun endSession(): RepositoryResult<SessionState> {
        return RepositoryResult.Data(bridge.endSession())
    }
}