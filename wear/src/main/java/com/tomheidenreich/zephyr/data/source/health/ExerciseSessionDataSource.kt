package com.tomheidenreich.zephyr.data.source.health

import com.tomheidenreich.zephyr.domain.model.SessionState
import kotlinx.coroutines.flow.Flow

interface ExerciseSessionDataSource {
    fun observeSessionState(): Flow<SessionState>

    suspend fun startSession(sessionName: String? = null): SessionState

    suspend fun pauseSession(): SessionState

    suspend fun resumeSession(): SessionState

    suspend fun endSession(): SessionState
}
