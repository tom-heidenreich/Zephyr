package com.tomheidenreich.zephyr.domain.session

import java.time.Duration
import java.time.Instant

enum class SessionStatus {
    IDLE,
    STARTING,
    ACTIVE,
    PAUSED,
    ENDED,
    ERROR,
}

data class SessionState(
    val id: String? = null,
    val status: SessionStatus = SessionStatus.IDLE,
    val startedAt: Instant? = null,
    val endedAt: Instant? = null,
    val elapsed: Duration = Duration.ZERO,
    val errorMessage: String? = null,
)