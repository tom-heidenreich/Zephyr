package com.tomheidenreich.zephyr.domain.repository

import com.tomheidenreich.zephyr.domain.surface.SurfaceSnapshot
import kotlinx.coroutines.flow.Flow

interface SurfaceSnapshotRepository {
    fun observeLatestSnapshot(): Flow<SurfaceSnapshot?>

    suspend fun upsertSnapshot(snapshot: SurfaceSnapshot)
}
