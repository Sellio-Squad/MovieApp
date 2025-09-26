package com.karrar.movieapp.ui.profile.settings.language

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.karrar.movieapp.R

enum class AppLanguages(
    val code: String,
    @DrawableRes val icon: Int,
    @StringRes val language: Int
) {
    Arabic(
        code = "ar",
        icon = R.drawable.colored_iraq_flag,
        language = R.string.arabic
    ),
    English(
        code = "en",
        icon = R.drawable.uk_flag,
        language = R.string.english
    )
}