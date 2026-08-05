package com.tomheidenreich.zephyr.data.repository.fake

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.repository.HeadingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeHeadingRepository : HeadingRepository {
    private val state = MutableStateFlow<RepositoryResult<Double>>(RepositoryResult.Data(90.0))

    override fun observeHeading(): Flow<RepositoryResult<Double>> = state.asStateFlow()

    override suspend fun clearHeading() {
        state.value = RepositoryResult.Loading
    }

    fun setHeadingDegrees(headingDegrees: Double?) {
        state.value = headingDegrees?.let { RepositoryResult.Data(it) } ?: RepositoryResult.Loading
    }
}