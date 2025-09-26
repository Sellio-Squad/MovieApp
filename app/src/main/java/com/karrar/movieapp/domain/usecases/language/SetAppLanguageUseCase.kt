package com.karrar.movieapp.domain.usecases.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.karrar.movieapp.data.repository.LanguageRepository
import javax.inject.Inject

class SetAppLanguageUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {
    suspend operator fun invoke(code: String) {
        languageRepository.saveLanguage(code)

        val tag = when (code.lowercase()) {
            "ar", "ar-sa" -> "ar-SA"
            "en", "en-us" -> "en-US"
            else -> "en-US"
        }
        val locales = LocaleListCompat.forLanguageTags(tag)
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
