package com.karrar.movieapp.ui.profile.settings.contentPreferences

sealed interface ContentPreferencesEvents {
    data object CloseDialogEvent : ContentPreferencesEvents
}
