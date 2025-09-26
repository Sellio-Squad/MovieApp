package com.karrar.movieapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.karrar.movieapp.data.repository.LanguageRepository
import com.karrar.movieapp.data.repository.ThemeRepository
import com.karrar.movieapp.utilities.Constants.THEME_DARK
import com.karrar.movieapp.utilities.ContentPreferencesManager
import com.karrar.movieapp.utilities.ImageFilterBindingAdapters
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class MovieApplication : Application() {

    @Inject lateinit var themeRepository: ThemeRepository
    @Inject lateinit var languageRepository: LanguageRepository
    @Inject lateinit var contentPreferencesManager: ContentPreferencesManager

    override fun onCreate() {
        super.onCreate()
        initializeTheme()
        initializeImageFilterConfig()
        applyAppLanguage()
    }

    private fun applyAppLanguage() {
        val code = languageRepository.getLanguage() ?: "en"
        val tag = when (code.lowercase()) { "ar", "ar-sa" -> "ar-SA"; "en", "en-us" -> "en-US"; else -> "en-US" }
        val locales = LocaleListCompat.forLanguageTags(tag)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    private fun initializeTheme() {
        runBlocking {
            try {
                val savedTheme = themeRepository.getCurrentTheme().first()
                if (savedTheme == THEME_DARK) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            } catch (_: Exception) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

    }
    private fun initializeImageFilterConfig() {
        // Initialize the binding adapter with the manager
        ImageFilterBindingAdapters.initialize(contentPreferencesManager)
    }
}
