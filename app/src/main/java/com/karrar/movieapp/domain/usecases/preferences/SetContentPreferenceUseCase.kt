package com.karrar.movieapp.domain.usecases.preferences

import com.karrar.movieapp.data.repository.ContentPreferencesRepository
import javax.inject.Inject

class SetContentPreferenceUseCase @Inject constructor(
    private val repository: ContentPreferencesRepository
) {
    suspend operator fun invoke(preference: String) {
        repository.setContentPreference(preference)
    }
}