package com.tomheidenreich.zephyr.data.source.wind

import com.tomheidenreich.zephyr.domain.wind.WindForecast
import com.tomheidenreich.zephyr.domain.wind.WindObservation

interface WindRemoteDataSource {
    suspend fun fetchCurrentWind(spotId: String): WindObservation

    suspend fun fetchForecast(spotId: String): WindForecast
}
