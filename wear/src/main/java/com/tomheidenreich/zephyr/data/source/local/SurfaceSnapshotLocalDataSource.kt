package com.tomheidenreich.zephyr.data.source.local

import com.tomheidenreich.zephyr.domain.surface.SurfaceSnapshot
import kotlinx.coroutines.flow.Flow

interface SurfaceSnapshotLocalDataSource {
    fun observeLatestSnapshot(): Flow<SurfaceSnapshot?>

    suspend fun upsert(snapshot: SurfaceSnapshot)
}
