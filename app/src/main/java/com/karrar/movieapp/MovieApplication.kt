package com.karrar.movieapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.karrar.movieapp.data.repository.ThemeRepository
import com.karrar.movieapp.utilities.Constants.THEME_DARK
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class MovieApplication : Application() {

    @Inject
    lateinit var themeRepository: ThemeRepository
    override fun onCreate() {
        super.onCreate()
        initializeTheme()
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
}