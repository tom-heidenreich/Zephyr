package com.tomheidenreich.zephyr.data.repository.weather

import com.tomheidenreich.zephyr.core.result.DataFreshness
import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.data.source.weather.OpenMeteoClient
import com.tomheidenreich.zephyr.data.source.weather.WeatherLocationProvider
import com.tomheidenreich.zephyr.domain.repository.WeatherRepository
import com.tomheidenreich.zephyr.domain.weather.WeatherSnapshot
import com.tomheidenreich.zephyr.domain.weather.WeatherSourceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class OpenMeteoWeatherRepository(
    private val locationProvider: WeatherLocationProvider,
    private val client: OpenMeteoClient,
    private val clock: Clock = Clock.systemUTC(),
) : WeatherRepository {
    private val state = MutableStateFlow<RepositoryResult<WeatherSnapshot>>(RepositoryResult.Loading)
    private val refreshMutex = Mutex()

    override fun observeWeather(): Flow<RepositoryResult<WeatherSnapshot>> = flow {
        if (shouldRefresh()) {
            refresh(force = false)
        }

        emitAll(state.asStateFlow())
    }

    override suspend fun refresh() {
        refresh(force = true)
    }

    private suspend fun refresh(force: Boolean) {
        refreshMutex.withLock {
            if (!force && !shouldRefresh()) {
                return
            }

            val location = locationProvider.getCurrentLocation()
            if (location == null) {
                val cached = state.value as? RepositoryResult.Data<WeatherSnapshot>
                state.value = cached?.copy(freshness = DataFreshness.STALE)
                    ?: RepositoryResult.Error(IllegalStateException("Current location unavailable"))
                return
            }

            try {
                val snapshot = client.fetchWeather(location)
                state.value = RepositoryResult.Data(snapshot, DataFreshness.FRESH)
            } catch (throwable: Throwable) {
                val cached = state.value as? RepositoryResult.Data<WeatherSnapshot>
                state.value = cached?.copy(freshness = DataFreshness.STALE)
                    ?: RepositoryResult.Error(throwable)
            }
        }
    }

    private fun shouldRefresh(now: Instant = clock.instant()): Boolean {
        val current = state.value as? RepositoryResult.Data<WeatherSnapshot> ?: return true
        return now >= current.value.validUntil || current.freshness == DataFreshness.STALE
    }
}
