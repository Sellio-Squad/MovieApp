package com.karrar.movieapp.ui.profile.login

sealed interface LogInEvents {
    object CloseDialogEvent : LogInEvents
    object LogInEvent : LogInEvents
    class GoToWebsiteEvent(val url: String) : LogInEvents
}