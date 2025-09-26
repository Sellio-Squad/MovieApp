package com.karrar.movieapp.ui.profile.settings.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.domain.usecases.language.GetAppLanguageUseCase
import com.karrar.movieapp.domain.usecases.language.SetAppLanguageUseCase
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeLanguageViewModel @Inject constructor(
    private val getLanguage: GetAppLanguageUseCase,
    private val setLanguage: SetAppLanguageUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(ChangeLanguageState(currentLanguage = AppLanguages.English))
    val uiState = _uiState.asStateFlow()

    private val _changeLanguageUIEvent: MutableStateFlow<Event<ChangeLanguageEvents?>> =
        MutableStateFlow(Event(null))
    val changeLanguageUIEvent = _changeLanguageUIEvent.asStateFlow()

    init {
        getCurrentLanguage()
    }

    private fun getCurrentLanguage() {
        val saved = getLanguage()
        val currentLanguage = AppLanguages.entries.find { it.code == saved } ?: AppLanguages.English
        _uiState.update { it.copy(currentLanguage = currentLanguage) }
    }

    fun onCloseDialog() {
        _changeLanguageUIEvent.update { Event(ChangeLanguageEvents.OnCloseDialog) }
    }

    fun onLanguageSelected(item: AppLanguages) {
        if (_uiState.value.currentLanguage != item) {
            _uiState.update { it.copy(currentLanguage = item) }
            viewModelScope.launch {
                setLanguage(item.code)
                _changeLanguageUIEvent.update { Event(ChangeLanguageEvents.OnLanguageChanged) }
                onCloseDialog()
            }
        } else {
            onCloseDialog()
        }
    }
}
