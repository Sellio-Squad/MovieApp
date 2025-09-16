package com.karrar.movieapp.ui.profile.settings.contentPreferences

data class ContentPreferencesUiState(
    val preferences: List<ContentPreferencesTypes> = ContentPreferencesTypes.entries,
    val selectedPreference: ContentPreferencesTypes = ContentPreferencesTypes.HideExplicit,
)
