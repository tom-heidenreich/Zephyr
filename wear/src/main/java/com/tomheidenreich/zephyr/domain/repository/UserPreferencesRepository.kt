package com.tomheidenreich.zephyr.domain.repository

import com.tomheidenreich.zephyr.domain.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun observePreferences(): Flow<UserPreferences>

    suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences)
}
