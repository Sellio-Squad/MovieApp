package com.karrar.movieapp.ui.profile.settings.contentPreferences

import androidx.lifecycle.ViewModel
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ContentPreferencesViewModel @Inject constructor() : ViewModel(),
    ContentPreferencesInteractionListener {
    private val _uiState = MutableStateFlow(ContentPreferencesUiState())
    val uiState = _uiState.asStateFlow()

    private val _preferenceUIEvent: MutableStateFlow<Event<ContentPreferencesEvents?>> =
        MutableStateFlow(Event(null))
    val preferenceUIEvent = _preferenceUIEvent.asStateFlow()

    fun onClose() {
        _preferenceUIEvent.update { Event(ContentPreferencesEvents.CloseDialogEvent) }

    }

    override fun onPreferenceSelected(item: ContentPreferencesTypes) {
        _uiState.update { it.copy(selectedPreference = item) }
    }

}