package com.karrar.movieapp.domain.usecases.preferences

import com.karrar.movieapp.data.repository.ContentPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContentPreferenceUseCase @Inject constructor(
    private val repository: ContentPreferencesRepository
) {
    operator fun invoke(): Flow<String> = repository.getContentPreference()
}