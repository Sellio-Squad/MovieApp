package com.karrar.movieapp.ui.profile.settings.contentPreferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.domain.usecases.preferences.GetContentPreferenceUseCase
import com.karrar.movieapp.domain.usecases.preferences.SetContentPreferenceUseCase
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentPreferencesViewModel @Inject constructor(
    private val getContentPreferenceUseCase: GetContentPreferenceUseCase,
    private val setContentPreferenceUseCase: SetContentPreferenceUseCase
) : ViewModel(), ContentPreferencesInteractionListener {

    private val _uiState = MutableStateFlow(ContentPreferencesUiState())
    val uiState = _uiState.asStateFlow()

    private val _preferenceUIEvent: MutableStateFlow<Event<ContentPreferencesEvents?>> =
        MutableStateFlow(Event(null))
    val preferenceUIEvent = _preferenceUIEvent.asStateFlow()

    init {
        loadCurrentPreference()
    }

    private fun loadCurrentPreference() {
        viewModelScope.launch {
            getContentPreferenceUseCase().collect { preferenceName ->
                val preference = ContentPreferencesTypes.entries.find { it.name == preferenceName }
                    ?: ContentPreferencesTypes.HideExplicit
                _uiState.update { it.copy(selectedPreference = preference) }
            }
        }
    }

    fun onClose() {
        _preferenceUIEvent.update { Event(ContentPreferencesEvents.CloseDialogEvent) }
    }

    override fun onPreferenceSelected(item: ContentPreferencesTypes) {
        viewModelScope.launch {
            setContentPreferenceUseCase(item.name)
            _uiState.update { it.copy(selectedPreference = item) }
            // Optionally close dialog after selection
            onClose()
        }
    }
}