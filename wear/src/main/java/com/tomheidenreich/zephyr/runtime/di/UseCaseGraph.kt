package com.tomheidenreich.zephyr.runtime.di

import com.tomheidenreich.zephyr.domain.usecase.BuildSurfaceSnapshotUseCase
import com.tomheidenreich.zephyr.domain.usecase.DeriveSailingMetricsUseCase

object UseCaseGraph {
    val deriveSailingMetricsUseCase: DeriveSailingMetricsUseCase by lazy { DeriveSailingMetricsUseCase() }
    val buildSurfaceSnapshotUseCase: BuildSurfaceSnapshotUseCase by lazy { BuildSurfaceSnapshotUseCase() }
}
