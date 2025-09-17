package com.karrar.movieapp.ui.login.signup

sealed interface SignUpEvents {
    object CloseDialogEvent : SignUpEvents
    class GoToWebsiteEvent(val url: String) : SignUpEvents
}