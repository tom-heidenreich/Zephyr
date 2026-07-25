package com.tomheidenreich.zephyr.data.source.health

import android.content.Context
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.ExerciseLapSummary
import com.tomheidenreich.zephyr.domain.model.TelemetryReading
import com.tomheidenreich.zephyr.domain.session.SessionState
import com.tomheidenreich.zephyr.domain.session.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.util.UUID
import androidx.health.services.client.endExercise
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise

class HealthServicesExerciseBridge(context: Context) {
    private val exerciseClient = HealthServices.getClient(context).exerciseClient
    private val sessionState = MutableStateFlow(SessionState())
    private val telemetry = MutableStateFlow<TelemetryReading?>(null)

    private val updateCallback = object : ExerciseUpdateCallback {
        override fun onRegistered() = Unit

        override fun onAvailabilityChanged(
            dataType: androidx.health.services.client.data.DataType<*, *>,
            availability: Availability
        ) = Unit

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) = Unit

        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val mappedStatus = update.exerciseStateInfo.state.toSessionStatus()
            val currentState = sessionState.value
            sessionState.value = currentState.copy(
                status = mappedStatus,
                startedAt = update.startTime ?: currentState.startedAt,
                endedAt = if (mappedStatus == SessionStatus.ENDED) Instant.now() else currentState.endedAt,
                errorMessage = null,
            )

            if (mappedStatus.isTrackingState()) {
                telemetry.value = telemetry.value.merge(update)
            }

            if (mappedStatus == SessionStatus.ENDED) {
                telemetry.value = null
            }
        }

        override fun onRegistrationFailed(throwable: Throwable) {
            sessionState.value = sessionState.value.copy(
                status = SessionStatus.ERROR,
                errorMessage = throwable.message ?: throwable::class.java.simpleName,
            )
        }
    }

    init {
        exerciseClient.setUpdateCallback(updateCallback)
    }

    fun observeSessionState(): StateFlow<SessionState> = sessionState.asStateFlow()

    fun observeTelemetry(): StateFlow<TelemetryReading?> = telemetry.asStateFlow()

    suspend fun startSession(sessionName: String?): SessionState {
        val startingState = SessionState(
            id = UUID.randomUUID().toString(),
            status = SessionStatus.STARTING,
            startedAt = Instant.now(),
        )
        sessionState.value = startingState

        return try {
            exerciseClient.startExercise(EXERCISE_CONFIG)
            val activeState = startingState.copy(status = SessionStatus.ACTIVE)
            sessionState.value = activeState
            activeState
        } catch (throwable: Throwable) {
            val errorState = startingState.copy(
                status = SessionStatus.ERROR,
                errorMessage = throwable.message ?: throwable::class.java.simpleName,
            )
            sessionState.value = errorState
            errorState
        }
    }

    suspend fun pauseSession(): SessionState {
        return try {
            exerciseClient.pauseExercise()
            sessionState.value = sessionState.value.copy(status = SessionStatus.PAUSED, errorMessage = null)
            sessionState.value
        } catch (throwable: Throwable) {
            sessionState.value = sessionState.value.copy(
                status = SessionStatus.ERROR,
                errorMessage = throwable.message ?: throwable::class.java.simpleName,
            )
            sessionState.value
        }
    }

    suspend fun resumeSession(): SessionState {
        return try {
            exerciseClient.resumeExercise()
            sessionState.value = sessionState.value.copy(status = SessionStatus.ACTIVE, errorMessage = null)
            sessionState.value
        } catch (throwable: Throwable) {
            sessionState.value = sessionState.value.copy(
                status = SessionStatus.ERROR,
                errorMessage = throwable.message ?: throwable::class.java.simpleName,
            )
            sessionState.value
        }
    }

    suspend fun endSession(): SessionState {
        return try {
            exerciseClient.endExercise()
            sessionState.value = sessionState.value.copy(
                status = SessionStatus.ENDED,
                endedAt = Instant.now(),
                errorMessage = null,
            )
            telemetry.value = null
            sessionState.value
        } catch (throwable: Throwable) {
            sessionState.value = sessionState.value.copy(
                status = SessionStatus.ERROR,
                errorMessage = throwable.message ?: throwable::class.java.simpleName,
            )
            sessionState.value
        }
    }

    fun clearTelemetry() {
        telemetry.value = null
    }

    private fun TelemetryReading?.merge(update: ExerciseUpdate): TelemetryReading {
        val metricsContainer = update.latestMetrics
        val heartRate = metricsContainer.getData(DataType.HEART_RATE_BPM).lastOrNull()?.value ?: this?.heartRateBpm
        val speed = metricsContainer.getData(DataType.SPEED).lastOrNull()?.value ?: this?.speedMps
        val distance = metricsContainer.getData(DataType.DISTANCE_TOTAL)?.total ?: this?.distanceMeters ?: 0.0

        return (this ?: TelemetryReading()).copy(
            timestamp = Instant.now(),
            heartRateBpm = heartRate,
            speedMps = speed,
            distanceMeters = distance,
        )
    }

    private fun ExerciseState.toSessionStatus(): SessionStatus {
        return when (this) {
            ExerciseState.ACTIVE, ExerciseState.USER_STARTING, ExerciseState.USER_RESUMING, ExerciseState.AUTO_RESUMING -> SessionStatus.ACTIVE
            ExerciseState.USER_PAUSED, ExerciseState.AUTO_PAUSED -> SessionStatus.PAUSED
            ExerciseState.USER_PAUSING, ExerciseState.AUTO_PAUSING, ExerciseState.PREPARING, ExerciseState.ENDING -> SessionStatus.STARTING
            ExerciseState.ENDED -> SessionStatus.ENDED
            else -> SessionStatus.IDLE
        }
    }

    private fun SessionStatus.isTrackingState(): Boolean {
        return this == SessionStatus.ACTIVE || this == SessionStatus.PAUSED || this == SessionStatus.STARTING
    }

    private companion object {
        val EXERCISE_CONFIG: ExerciseConfig = ExerciseConfig.Builder(ExerciseType.SAILING)
            .setDataTypes(
                setOf(
                    DataType.HEART_RATE_BPM,
                    DataType.SPEED,
                    DataType.DISTANCE_TOTAL,
                )
            )
            .setIsAutoPauseAndResumeEnabled(false)
            .setIsGpsEnabled(false)
            .build()
    }
}