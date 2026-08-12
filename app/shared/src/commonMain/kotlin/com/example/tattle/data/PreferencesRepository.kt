package com.example.tattle.data

import com.example.tattle.models.NotificationsPrefs
import com.example.tattle.models.UserPreferences
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface PreferencesRepository {
    val userPreferences: StateFlow<UserPreferences>
    suspend fun updatePreferences(preferences: UserPreferences)
    suspend fun clearPreferences()
}

class SettingsPreferencesRepository(
    private val settings: Settings
) : PreferencesRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val _userPreferences = MutableStateFlow(loadPreferences())
    override val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        val jsonString = settings.getStringOrNull(PREF_USER_DATA)
        return if (jsonString != null) {
            try {
                json.decodeFromString<UserPreferences>(jsonString)
            } catch (e: Exception) {
                DEFAULT_PREFERENCES
            }
        } else {
            DEFAULT_PREFERENCES
        }
    }

    override suspend fun updatePreferences(preferences: UserPreferences) {
        settings[PREF_USER_DATA] = json.encodeToString(preferences)
        _userPreferences.value = preferences
    }

    override suspend fun clearPreferences() {
        settings.clear()
        _userPreferences.value = DEFAULT_PREFERENCES
    }

    companion object {
        private const val PREF_USER_DATA = "user_data"
        
        val DEFAULT_PREFERENCES = UserPreferences(
            isOnboarded = false,
            ageGroup = "",
            interests = emptyList(),
            language = "English",
            notifications = NotificationsPrefs(true, true, true),
            streak = 0,
            totalCardsRead = 0,
            readHistory = emptyList(),
            bookmarks = emptyList(),
            reactions = emptyMap(),
            adFreeUntil = null,
            sensitivity = "Standard",
            lastReadDate = null
        )
    }
}
