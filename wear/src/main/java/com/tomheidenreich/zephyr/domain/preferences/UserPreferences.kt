package com.tomheidenreich.zephyr.domain.preferences

enum class UnitSystem {
    METRIC,
    IMPERIAL,
}

data class UserPreferences(
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val preferredSpotId: String? = null,
    val showHeartRate: Boolean = true,
    val showHeading: Boolean = true,
)