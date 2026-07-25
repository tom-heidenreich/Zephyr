package com.tomheidenreich.zephyr.data.repository.fake

import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.wind.WindDataSourceType
import com.tomheidenreich.zephyr.domain.wind.WindForecast
import com.tomheidenreich.zephyr.domain.wind.WindForecastEntry
import com.tomheidenreich.zephyr.domain.wind.WindReference
import com.tomheidenreich.zephyr.domain.wind.WindObservation
import com.tomheidenreich.zephyr.domain.wind.WindSample
import com.tomheidenreich.zephyr.domain.repository.WindRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

class FakeWindRepository : WindRepository {
    private val observationState = MutableStateFlow<RepositoryResult<WindObservation>>(
        RepositoryResult.Loading
    )
    private val forecastState = MutableStateFlow<RepositoryResult<WindForecast>>(
        RepositoryResult.Loading
    )

    override fun observeCurrentWind(spotId: String): Flow<RepositoryResult<WindObservation>> {
        if (observationState.value == RepositoryResult.Loading) {
            observationState.value = RepositoryResult.Data(
                WindObservation(
                    spotId = spotId,
                    observedAt = Instant.now(),
                    trueWind = WindSample(speedKts = 18.0, directionFromDegrees = 270, reference = WindReference.TRUE),
                    apparentWind = WindSample(
                        speedKts = 22.0,
                        directionFromDegrees = 255,
                        reference = WindReference.APPARENT
                    ),
                    sourceType = WindDataSourceType.FUSED,
                    source = "fake",
                )
            )
        }
        return observationState.asStateFlow()
    }

    override fun observeForecast(spotId: String): Flow<RepositoryResult<WindForecast>> {
        if (forecastState.value == RepositoryResult.Loading) {
            val now = Instant.now()
            forecastState.value = RepositoryResult.Data(
                WindForecast(
                    spotId = spotId,
                    generatedAt = now,
                    entries = listOf(
                        WindForecastEntry(
                            at = now.plusSeconds(3600),
                            speedKts = 17.0,
                            gustKts = 22.0,
                            directionDegrees = 275
                        ),
                        WindForecastEntry(
                            at = now.plusSeconds(7200),
                            speedKts = 19.0,
                            gustKts = 24.0,
                            directionDegrees = 280
                        ),
                    ),
                    source = "fake",
                )
            )
        }
        return forecastState.asStateFlow()
    }

    override suspend fun refresh(spotId: String) {
        observeCurrentWind(spotId)
        observeForecast(spotId)
    }
}
