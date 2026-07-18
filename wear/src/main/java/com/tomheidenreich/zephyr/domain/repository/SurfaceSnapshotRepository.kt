package com.tomheidenreich.zephyr.domain.repository

import com.tomheidenreich.zephyr.domain.model.SurfaceSnapshot
import kotlinx.coroutines.flow.Flow

interface SurfaceSnapshotRepository {
    fun observeLatestSnapshot(): Flow<SurfaceSnapshot?>

    suspend fun upsertSnapshot(snapshot: SurfaceSnapshot)
}
