package com.karrar.movieapp.domain.usecases.theme

import com.karrar.movieapp.data.repository.ThemeRepository
import jakarta.inject.Inject

class ChangeThemeUseCase @Inject constructor(private val themeRepository: ThemeRepository) {
    suspend fun changeTheme(theme: String) {
        themeRepository.changeTheme(theme)
    }
}