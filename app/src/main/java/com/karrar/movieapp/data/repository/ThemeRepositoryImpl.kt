package com.karrar.movieapp.data.repository

import androidx.appcompat.app.AppCompatDelegate
import com.karrar.movieapp.data.local.DataStorePreferences
import com.karrar.movieapp.utilities.Constants.APP_THEME
import com.karrar.movieapp.utilities.Constants.THEME_DARK
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeRepositoryImpl @Inject constructor(private val dataStorePreferences: DataStorePreferences) :
    ThemeRepository {
    override suspend fun changeTheme(theme: String) {
        saveTheme(theme)
        if (theme == THEME_DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private suspend fun saveTheme(theme: String) {
        dataStorePreferences.writeString(APP_THEME, theme)
    }

    override suspend fun getCurrentTheme(): Flow<String> =
        dataStorePreferences.readStringFlow(APP_THEME).map { it ?: THEME_DARK }
}