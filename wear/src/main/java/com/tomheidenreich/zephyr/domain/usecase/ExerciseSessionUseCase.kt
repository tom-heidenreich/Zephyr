package com.tomheidenreich.zephyr.domain.usecase

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.repository.ExerciseSessionRepository
import com.tomheidenreich.zephyr.domain.session.SessionState
import kotlinx.coroutines.flow.Flow

class ExerciseSessionUseCase(
    private val exerciseSessionRepository: ExerciseSessionRepository,
) {
    fun observeSessionState(): Flow<RepositoryResult<SessionState>> {
        return exerciseSessionRepository.observeSessionState()
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