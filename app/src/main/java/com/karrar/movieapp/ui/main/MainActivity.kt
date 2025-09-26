package com.karrar.movieapp.ui.main

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.ActivityMainBinding
import com.karrar.movieapp.ui.base.BottomNavigationController
import com.karrar.movieapp.data.repository.AccountRepository
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), BottomNavigationController {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var accountRepository: AccountRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        setTheme(R.style.Theme_MovieApp)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        installSplashScreen()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(updateBaseContextLocale(newBase))
    }

    private fun updateBaseContextLocale(context: Context?): Context? {
        if (context == null) return null

        val savedLanguage = if (::accountRepository.isInitialized) {
            accountRepository.getLanguage() ?: "en"
        } else {
            val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            preferences.getString("language", "en") ?: "en"
        }

        return setLocale(context, savedLanguage)
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (::accountRepository.isInitialized) {
            val savedLanguage = accountRepository.getLanguage() ?: "en"
            val locale = Locale(savedLanguage)
            Locale.setDefault(locale)

            val configuration = Configuration(newConfig)
            configuration.setLocale(locale)

            if (savedLanguage == "ar") {
                configuration.setLayoutDirection(locale)
            }

            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }

    override fun onResume() {
        super.onResume()
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.exploringFragment,
                R.id.matchFragment,
                R.id.profileFragment,
            )
        )
        val navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavigation.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

        setBottomNavigationVisibility(navController)
        setNavigationController(navController)
    }

    private fun setBottomNavigationVisibility(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.isVisible = destination.id != R.id.loginFragment
        }
    }

    private fun setNavigationController(navController: NavController) {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(item, navController)
            navController.popBackStack(item.itemId, inclusive = false)
            true
        }
    }

    fun hideBottomNavigation() {
        binding.bottomNavigation.isVisible = false
    }

    fun showBottomNavigation() {
        binding.bottomNavigation.isVisible = true
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp() || super.onSupportNavigateUp()
    }

    override fun showBottomNavigation(show: Boolean) {
        binding.bottomNavigation.isVisible = show
    }
}