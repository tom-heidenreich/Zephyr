package com.tomheidenreich.zephyr.runtime.di

import com.tomheidenreich.zephyr.domain.usecase.DashboardUseCase
import com.tomheidenreich.zephyr.domain.usecase.BuildSurfaceSnapshotUseCase
import com.tomheidenreich.zephyr.domain.usecase.DeriveSailingMetricsUseCase

object UseCaseGraph {
    val dashboardUseCase: DashboardUseCase by lazy {
        DashboardUseCase(
            exerciseSessionRepository = AppGraph.exerciseSessionRepository,
            liveMetricsRepository = AppGraph.liveMetricsRepository,
            windRepository = AppGraph.windRepository,
            surfaceSnapshotRepository = AppGraph.surfaceSnapshotRepository,
        )
    }

    val deriveSailingMetricsUseCase: DeriveSailingMetricsUseCase by lazy { DeriveSailingMetricsUseCase() }
    val buildSurfaceSnapshotUseCase: BuildSurfaceSnapshotUseCase by lazy { BuildSurfaceSnapshotUseCase() }
}
