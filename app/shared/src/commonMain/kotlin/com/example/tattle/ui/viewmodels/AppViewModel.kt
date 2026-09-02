package com.example.tattle.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tattle.data.PreferencesRepository
import com.example.tattle.models.UserPreferences
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: PreferencesRepository,
    private val auth: Auth
) : ViewModel() {

    private val _currentFeedTab = MutableStateFlow<String?>(null)
    val currentFeedTab = _currentFeedTab.asStateFlow()

    val preferences = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sessionStatus: StateFlow<SessionStatus> = auth.sessionStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionStatus.Initializing)

    fun updatePreferences(newPrefs: UserPreferences) {
        viewModelScope.launch {
            repository.updatePreferences(newPrefs)
        }
    }

    fun setFeedTab(tab: String) {
        _currentFeedTab.value = tab
    }

    fun purgeData() {
        viewModelScope.launch {
            repository.clearPreferences()
        }
    }
}
