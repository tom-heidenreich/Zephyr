package com.tomheidenreich.zephyr.domain.repository

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import kotlinx.coroutines.flow.Flow

interface HeadingRepository {
    fun observeHeading(): Flow<RepositoryResult<Double>>

    suspend fun clearHeading()
}