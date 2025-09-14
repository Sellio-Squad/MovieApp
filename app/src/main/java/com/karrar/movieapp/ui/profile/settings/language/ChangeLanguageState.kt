package com.karrar.movieapp.ui.profile.settings.language

data class ChangeLanguageState(
    val currentLanguage: AppLanguages,
    val languages: List<AppLanguages> = AppLanguages.entries
)