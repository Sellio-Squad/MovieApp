package com.karrar.movieapp

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.karrar.movieapp.data.repository.AccountRepository
import com.karrar.movieapp.data.repository.ThemeRepository
import com.karrar.movieapp.utilities.Constants.THEME_DARK
import com.karrar.movieapp.utilities.ContentPreferencesManager
import com.karrar.movieapp.utilities.ImageFilterBindingAdapters
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

@HiltAndroidApp
class MovieApplication : Application() {

    @Inject
    lateinit var themeRepository: ThemeRepository

    @Inject
    lateinit var contentPreferencesManager: ContentPreferencesManager

    @Inject
    lateinit var repository: AccountRepository

    override fun onCreate() {
        super.onCreate()
        initializeTheme()
        initializeImageFilterConfig()
        applyLanguage()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(updateBaseContextLocale(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::repository.isInitialized) {
            applyLanguage()
        }
    }

    private fun updateBaseContextLocale(context: Context?): Context? {
        if (context == null) return null

        val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val savedLanguage = preferences.getString("language", "en") ?: "en"

        return setLocale(context, savedLanguage)
    }

    private fun applyLanguage() {
        if (::repository.isInitialized) {
            val savedLanguage = repository.getLanguage() ?: "en"
            val updatedContext = setLocale(this, savedLanguage)

            val config = updatedContext.resources.configuration
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }

    private fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        if (languageCode == "ar") {
            configuration.setLayoutDirection(locale)
        }

        return context.createConfigurationContext(configuration)
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
            } catch (e: Exception) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun initializeImageFilterConfig() {
        // Initialize the binding adapter with the manager
        ImageFilterBindingAdapters.initialize(contentPreferencesManager)
    }
}