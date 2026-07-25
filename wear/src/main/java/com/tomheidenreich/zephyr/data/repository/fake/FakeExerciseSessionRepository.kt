package com.tomheidenreich.zephyr.data.repository.fake

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.session.SessionState
import com.tomheidenreich.zephyr.domain.session.SessionStatus
import com.tomheidenreich.zephyr.domain.repository.ExerciseSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.util.UUID

class FakeExerciseSessionRepository : ExerciseSessionRepository {
    private val state = MutableStateFlow<RepositoryResult<SessionState>>(RepositoryResult.Loading)

    init {
        state.value = RepositoryResult.Data(SessionState())
    }

    override fun observeSessionState(): Flow<RepositoryResult<SessionState>> = state.asStateFlow()

    override suspend fun startSession(sessionName: String?): RepositoryResult<SessionState> {
        val active = SessionState(
            id = UUID.randomUUID().toString(),
            status = SessionStatus.ACTIVE,
            startedAt = Instant.now(),
        )
        return RepositoryResult.Data(active).also { state.value = it }
    }

    override suspend fun pauseSession(): RepositoryResult<SessionState> {
        val current = (state.value as? RepositoryResult.Data)?.value ?: SessionState()
        val paused = current.copy(status = SessionStatus.PAUSED)
        return RepositoryResult.Data(paused).also { state.value = it }
    }

    override suspend fun resumeSession(): RepositoryResult<SessionState> {
        val current = (state.value as? RepositoryResult.Data)?.value ?: SessionState()
        val resumed = current.copy(status = SessionStatus.ACTIVE)
        return RepositoryResult.Data(resumed).also { state.value = it }
    }

    override suspend fun endSession(): RepositoryResult<SessionState> {
        val current = (state.value as? RepositoryResult.Data)?.value ?: SessionState()
        val ended = current.copy(status = SessionStatus.ENDED, endedAt = Instant.now())
        return RepositoryResult.Data(ended).also { state.value = it }
    }
}
