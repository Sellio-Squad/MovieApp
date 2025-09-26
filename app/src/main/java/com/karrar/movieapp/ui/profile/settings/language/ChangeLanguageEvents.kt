package com.karrar.movieapp.ui.profile.settings.language

sealed interface ChangeLanguageEvents {
    data object OnCloseDialog : ChangeLanguageEvents
    data object OnLanguageChanged : ChangeLanguageEvents
}