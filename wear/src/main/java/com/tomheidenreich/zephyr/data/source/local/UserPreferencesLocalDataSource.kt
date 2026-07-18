package com.tomheidenreich.zephyr.data.source.local

import com.tomheidenreich.zephyr.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesLocalDataSource {
    fun observePreferences(): Flow<UserPreferences>

    suspend fun update(transform: (UserPreferences) -> UserPreferences)
}
