package com.tomheidenreich.zephyr.data.repository.weather

import com.tomheidenreich.zephyr.core.result.DataFreshness
import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.data.source.weather.OpenMeteoClient
import com.tomheidenreich.zephyr.data.source.weather.WeatherLocationProvider
import com.tomheidenreich.zephyr.domain.weather.WeatherCellSelection
import com.tomheidenreich.zephyr.domain.weather.WeatherLocation
import com.tomheidenreich.zephyr.domain.weather.WeatherSnapshot
import com.tomheidenreich.zephyr.domain.weather.WeatherSourceType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class OpenMeteoWeatherRepositoryTest {
    @Test
    fun `does not refetch while cached snapshot is still fresh`() = runBlocking {
        val clock = fixedClock("2026-08-04T10:07:00Z")
        val location = testLocation()
        val client = CountingOpenMeteoClient(
            snapshot = testSnapshot(location, validUntil = Instant.parse("2026-08-04T10:15:00Z")),
        )
        val repository = OpenMeteoWeatherRepository(
            locationProvider = FixedLocationProvider(location),
            client = client,
            clock = clock,
        )

        val first = repository.observeWeather().first()
        val second = repository.observeWeather().first()

        assertTrue(first is RepositoryResult.Data)
        assertTrue(second is RepositoryResult.Data)
        assertEquals(1, client.calls)
    }

    @Test
    fun `refetches once the cached slice expires`() = runBlocking {
        val startClock = mutableClock("2026-08-04T10:07:00Z")
        val location = testLocation()
        val firstSnapshot = testSnapshot(location, validUntil = Instant.parse("2026-08-04T10:15:00Z"))
        val secondSnapshot = firstSnapshot.copy(
            observedAt = Instant.parse("2026-08-04T10:15:01Z"),
            validFrom = Instant.parse("2026-08-04T10:15:00Z"),
            validUntil = Instant.parse("2026-08-04T10:30:00Z"),
            source = "refreshed",
        )
        val client = SequencedOpenMeteoClient(listOf(firstSnapshot, secondSnapshot))
        val repository = OpenMeteoWeatherRepository(
            locationProvider = FixedLocationProvider(location),
            client = client,
            clock = startClock,
        )

        val initial = repository.observeWeather().first()
        assertTrue(initial is RepositoryResult.Data)

        startClock.set("2026-08-04T10:15:01Z")
        val refreshed = repository.observeWeather().first()

        assertTrue(refreshed is RepositoryResult.Data)
        assertEquals(2, client.calls)
        assertEquals(DataFreshness.FRESH, (refreshed as RepositoryResult.Data).freshness)
        assertEquals("refreshed", refreshed.value.source)
    }

    @Test
    fun `marks cached data stale when location is unavailable`() = runBlocking {
        val clock = fixedClock("2026-08-04T10:07:00Z")
        val location = testLocation()
        val client = CountingOpenMeteoClient(
            snapshot = testSnapshot(
                location,
                validUntil = Instant.parse("2026-08-04T10:15:00Z")
            )
        )
        val repository = OpenMeteoWeatherRepository(
            locationProvider = FixedLocationProvider(null),
            client = client,
            clock = clock,
        )

        repository.refresh()

        val result = repository.observeWeather().first()
        assertTrue(result is RepositoryResult.Error)
    }

    private fun testLocation() = WeatherLocation(
        latitude = 52.52,
        longitude = 13.41,
        timezone = "Europe/Berlin",
        cellSelection = WeatherCellSelection.NEAREST,
    )

    private fun testSnapshot(location: WeatherLocation, validUntil: Instant) = WeatherSnapshot(
        location = location,
        observedAt = Instant.parse("2026-08-04T10:00:00Z"),
        validFrom = Instant.parse("2026-08-04T10:00:00Z"),
        validUntil = validUntil,
        sourceType = WeatherSourceType.FORECAST,
        source = "open-meteo",
        windSpeedKts = 18.0,
        windDirectionDegrees = 270,
    )

    private fun fixedClock(instant: String): Clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC)

    private class MutableClock(private var instant: Instant) : Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?) = this
        override fun instant(): Instant = instant
        fun set(value: String) {
            instant = Instant.parse(value)
        }
    }

    private fun mutableClock(instant: String) = MutableClock(Instant.parse(instant))

    private class FixedLocationProvider(private val location: WeatherLocation?) : WeatherLocationProvider {
        override suspend fun getCurrentLocation(): WeatherLocation? = location
    }

    private class CountingOpenMeteoClient(private val snapshot: WeatherSnapshot) : OpenMeteoClient {
        var calls: Int = 0
            private set

        override suspend fun fetchWeather(location: WeatherLocation): WeatherSnapshot {
            calls += 1
            return snapshot.copy(location = location)
        }
    }

    private class SequencedOpenMeteoClient(private val snapshots: List<WeatherSnapshot>) : OpenMeteoClient {
        var calls: Int = 0
            private set

        override suspend fun fetchWeather(location: WeatherLocation): WeatherSnapshot {
            val index = calls.coerceAtMost(snapshots.lastIndex)
            calls += 1
            return snapshots[index].copy(location = location)
        }
    }
}
