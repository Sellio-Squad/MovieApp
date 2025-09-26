package com.karrar.movieapp.domain.usecases.language

import com.karrar.movieapp.data.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAppLanguageUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {
    operator fun invoke(): Flow<String?> = languageRepository.observeLanguage()
}
