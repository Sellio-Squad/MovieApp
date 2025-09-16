package com.karrar.movieapp.utilities

import com.karrar.movieapp.domain.usecases.preferences.GetContentPreferenceUseCase
import com.karrar.movieapp.ui.profile.settings.contentPreferences.ContentPreferencesTypes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentPreferencesManager @Inject constructor(
    private val getContentPreferenceUseCase: GetContentPreferenceUseCase
) {
    val contentPreference: Flow<ContentPreferencesTypes> =
        getContentPreferenceUseCase().map { preferenceName ->
            ContentPreferencesTypes.entries.find { it.name == preferenceName }
                ?: ContentPreferencesTypes.HideExplicit
        }

    suspend fun getCurrentPreference(): ContentPreferencesTypes {
        val preferenceName = getContentPreferenceUseCase().map { it }.first()
        return ContentPreferencesTypes.entries.find { it.name == preferenceName }
            ?: ContentPreferencesTypes.HideExplicit
    }
}