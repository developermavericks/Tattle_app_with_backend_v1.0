package com.example.tattle.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tattle.data.PreferencesRepository
import com.example.tattle.data.UserSync
import com.example.tattle.models.UserPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: PreferencesRepository,
    private val userSync: UserSync
) : ViewModel() {

    /** Emitted when the account was deleted on the server (or the login expired): the UI goes back to the login screen. */
    val sessionEnded = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        viewModelScope.launch {
            userSync.sessionExpired.collect {
                repository.clearPreferences()          // forget the profile, so the person has to log in and onboard again
                userSync.ensureSession()
                sessionEnded.emit(Unit)
            }
        }
        viewModelScope.launch {
            userSync.validateSession()
            userSync.ensureSession()
            userSync.pullBookmarks()
        }
    }

    private val _currentFeedTab = MutableStateFlow<String?>(null)
    val currentFeedTab = _currentFeedTab.asStateFlow()

    val preferences = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updatePreferences(newPrefs: UserPreferences) {
        userSync.onPreferencesChanged(repository.userPreferences.value, newPrefs)     // views, reads, bookmarks, likes -> the user's account on the server
        viewModelScope.launch {
            repository.updatePreferences(newPrefs)
        }
    }

    fun setFeedTab(tab: String) {
        _currentFeedTab.value = tab
    }

    fun purgeData() {
        viewModelScope.launch {
            userSync.logout()
            repository.clearPreferences()
        }
    }
}
