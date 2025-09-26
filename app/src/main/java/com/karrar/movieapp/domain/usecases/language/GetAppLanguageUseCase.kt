package com.karrar.movieapp.domain.usecases.language

import com.karrar.movieapp.data.repository.LanguageRepository
import javax.inject.Inject

class GetAppLanguageUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {
    operator fun invoke(): String = languageRepository.getLanguage() ?: "en"
}
