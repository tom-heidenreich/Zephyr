package com.tomheidenreich.zephyr.domain.repository

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.model.SessionState
import kotlinx.coroutines.flow.Flow

interface ExerciseSessionRepository {
    fun observeSessionState(): Flow<RepositoryResult<SessionState>>

    suspend fun startSession(sessionName: String? = null): RepositoryResult<SessionState>

    suspend fun pauseSession(): RepositoryResult<SessionState>

    suspend fun resumeSession(): RepositoryResult<SessionState>

    suspend fun endSession(): RepositoryResult<SessionState>
}
