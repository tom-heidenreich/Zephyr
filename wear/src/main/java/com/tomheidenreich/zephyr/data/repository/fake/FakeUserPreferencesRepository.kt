package com.tomheidenreich.zephyr.data.repository.fake

import com.tomheidenreich.zephyr.domain.preferences.UserPreferences
import com.tomheidenreich.zephyr.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUserPreferencesRepository : UserPreferencesRepository {
    private val state = MutableStateFlow(UserPreferences())

    override fun observePreferences(): Flow<UserPreferences> = state.asStateFlow()

    override suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        state.value = transform(state.value)
    }
}
