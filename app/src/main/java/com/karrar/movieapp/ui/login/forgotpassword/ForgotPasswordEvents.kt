package com.karrar.movieapp.ui.login.forgotpassword

sealed interface ForgotPasswordEvents {
    object CloseDialogEvent : ForgotPasswordEvents
    class GoToWebsiteEvent(val url: String) : ForgotPasswordEvents
}