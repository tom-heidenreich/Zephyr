package com.tomheidenreich.zephyr.runtime.di

import com.tomheidenreich.zephyr.domain.usecase.BuildAppSurfaceSnapshotUseCase
import com.tomheidenreich.zephyr.domain.usecase.DeriveSailingMetricsUseCase
import com.tomheidenreich.zephyr.domain.usecase.ExerciseSessionUseCase
import com.tomheidenreich.zephyr.domain.usecase.TelemetryStreamUseCase
import com.tomheidenreich.zephyr.domain.usecase.WeatherStreamUseCase

object UseCaseGraph {
    val exerciseSessionUseCase: ExerciseSessionUseCase by lazy {
        ExerciseSessionUseCase(
            exerciseSessionRepository = AppGraph.exerciseSessionRepository,
        )
    }

    val telemetryStreamUseCase: TelemetryStreamUseCase by lazy {
        TelemetryStreamUseCase(
            telemetryRepository = AppGraph.telemetryRepository,
            headingRepository = AppGraph.headingRepository,
        )
    }

    val weatherStreamUseCase: WeatherStreamUseCase by lazy {
        WeatherStreamUseCase(
            weatherRepository = AppGraph.weatherRepository,
        )
    }

    val deriveSailingMetricsUseCase: DeriveSailingMetricsUseCase by lazy { DeriveSailingMetricsUseCase() }
    val buildSurfaceSnapshotUseCase: BuildAppSurfaceSnapshotUseCase by lazy { BuildAppSurfaceSnapshotUseCase() }
}
