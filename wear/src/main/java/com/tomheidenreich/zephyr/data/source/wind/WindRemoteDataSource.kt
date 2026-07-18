package com.tomheidenreich.zephyr.data.source.wind

import com.tomheidenreich.zephyr.domain.model.WindForecast
import com.tomheidenreich.zephyr.domain.model.WindObservation

interface WindRemoteDataSource {
    suspend fun fetchCurrentWind(spotId: String): WindObservation

    suspend fun fetchForecast(spotId: String): WindForecast
}
