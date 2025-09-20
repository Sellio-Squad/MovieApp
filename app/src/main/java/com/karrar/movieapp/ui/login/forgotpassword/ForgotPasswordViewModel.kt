package com.karrar.movieapp.ui.login.forgotpassword

import androidx.lifecycle.ViewModel
import com.karrar.movieapp.BuildConfig
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor() : ViewModel() {

    private val _forgotPasswordUIEvent: MutableStateFlow<Event<ForgotPasswordEvents?>> =
        MutableStateFlow(Event(null))
    val forgotPasswordUIEvent = _forgotPasswordUIEvent.asStateFlow()

    fun onGoToWebsiteClicked() {
        _forgotPasswordUIEvent.update {
            Event(ForgotPasswordEvents.GoToWebsiteEvent(BuildConfig.TMDB_FORGOT_PASSWORD_URL))
        }
    }

    fun onCancelClicked() {
        _forgotPasswordUIEvent.update { Event(ForgotPasswordEvents.CloseDialogEvent) }
    }
}