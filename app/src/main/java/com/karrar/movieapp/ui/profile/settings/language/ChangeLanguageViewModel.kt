package com.karrar.movieapp.ui.profile.settings.language

import androidx.lifecycle.ViewModel
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ChangeLanguageViewModel @Inject constructor() : ViewModel() {
    private val _uiState =
        MutableStateFlow(ChangeLanguageState(currentLanguage = AppLanguages.English))
    val uiState = _uiState.asStateFlow()
    private val _changeLanguageUIEvent: MutableStateFlow<Event<ChangeLanguageEvents?>> =
        MutableStateFlow(Event(null))
    val changeLanguageUIEvent = _changeLanguageUIEvent.asStateFlow()

    fun onCloseDialog() {
        _changeLanguageUIEvent.update { Event(ChangeLanguageEvents.OnCloseDialog) }
    }

    fun onLanguageSelected(item: AppLanguages) {
        _uiState.update { it.copy(currentLanguage = item) }
    }
}