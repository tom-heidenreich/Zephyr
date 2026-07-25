package com.tomheidenreich.zephyr.runtime.di

import com.tomheidenreich.zephyr.domain.usecase.BuildSurfaceSnapshotUseCase
import com.tomheidenreich.zephyr.domain.usecase.DeriveSailingMetricsUseCase
import com.tomheidenreich.zephyr.domain.usecase.ExerciseSessionUseCase
import com.tomheidenreich.zephyr.domain.usecase.TelemetryStreamUseCase
import com.tomheidenreich.zephyr.domain.usecase.WindStreamUseCase

object UseCaseGraph {
    val exerciseSessionUseCase: ExerciseSessionUseCase by lazy {
        ExerciseSessionUseCase(
            exerciseSessionRepository = AppGraph.exerciseSessionRepository,
        )
    }

    val telemetryStreamUseCase: TelemetryStreamUseCase by lazy {
        TelemetryStreamUseCase(
            telemetryRepository = AppGraph.telemetryRepository,
        )
    }

    val windStreamUseCase: WindStreamUseCase by lazy {
        WindStreamUseCase(
            windRepository = AppGraph.windRepository,
        )
    }

    val deriveSailingMetricsUseCase: DeriveSailingMetricsUseCase by lazy { DeriveSailingMetricsUseCase() }
    val buildSurfaceSnapshotUseCase: BuildSurfaceSnapshotUseCase by lazy { BuildSurfaceSnapshotUseCase() }
}
