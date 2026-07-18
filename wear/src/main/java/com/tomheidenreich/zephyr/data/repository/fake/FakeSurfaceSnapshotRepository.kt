package com.tomheidenreich.zephyr.data.repository.fake

import com.tomheidenreich.zephyr.domain.model.SurfaceSnapshot
import com.tomheidenreich.zephyr.domain.repository.SurfaceSnapshotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSurfaceSnapshotRepository : SurfaceSnapshotRepository {
    private val state = MutableStateFlow<SurfaceSnapshot?>(null)

    override fun observeLatestSnapshot(): Flow<SurfaceSnapshot?> = state.asStateFlow()

    override suspend fun upsertSnapshot(snapshot: SurfaceSnapshot) {
        state.value = snapshot
    }
}
