package com.karrar.movieapp.ui.login.signup

import androidx.lifecycle.ViewModel
import com.karrar.movieapp.BuildConfig
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {

    private val _signUpUIEvent: MutableStateFlow<Event<SignUpEvents?>> =
        MutableStateFlow(Event(null))
    val signUpUIEvent = _signUpUIEvent.asStateFlow()

    fun onGoToWebsiteClicked() {
        _signUpUIEvent.update {
            Event(SignUpEvents.GoToWebsiteEvent(BuildConfig.TMDB_SIGNUP_URL))
        }
    }

    fun onCancelClicked() {
        _signUpUIEvent.update { Event(SignUpEvents.CloseDialogEvent) }
    }
}