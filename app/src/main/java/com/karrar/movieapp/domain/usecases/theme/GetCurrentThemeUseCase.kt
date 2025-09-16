package com.karrar.movieapp.domain.usecases.theme

import com.karrar.movieapp.data.repository.ThemeRepository
import jakarta.inject.Inject

class GetCurrentThemeUseCase @Inject constructor(private val themeRepository: ThemeRepository) {
    suspend fun getCurrentTheme() = themeRepository.getCurrentTheme()

}