package com.karrar.movieapp.ui.profile.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.karrar.movieapp.domain.usecases.CheckIfLoggedInUseCase
import com.karrar.movieapp.utilities.ErrorUI.EDIT_PROFILE_URL
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(
    state: SavedStateHandle,
    private val checkIfLoggedInUseCase: CheckIfLoggedInUseCase
) : ViewModel() {
    val args = LogInDialogArgs.fromSavedStateHandle(state)

    private val _logoInUIEvent: MutableStateFlow<Event<LogInEvents?>> =
        MutableStateFlow(Event(null))
    val loginUIEvent = _logoInUIEvent.asStateFlow()

    val isLoggedIn = MutableStateFlow(checkIfLoggedInUseCase())

    fun onActionClicked() {
        if (isLoggedIn.value) {
            _logoInUIEvent.update { Event(LogInEvents.GoToWebsiteEvent(EDIT_PROFILE_URL + args.userName)) }
        } else {
            _logoInUIEvent.update { Event(LogInEvents.LogInEvent) }
        }
    }

    fun onCloseDialog() {
        _logoInUIEvent.update { Event(LogInEvents.CloseDialogEvent) }
    }

}