package com.karrar.movieapp.ui.profile.settings.contentPreferences

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.karrar.movieapp.R

enum class ContentPreferencesTypes(
    val blur: String,
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val icon: Int
) {
    HideExplicit(
        blur = "high",
        title = R.string.hide_explicit_content,
        description = R.string.hides_revealing_or_inappropriate_posters_e_g_nudity_strong_sexual_content,
        icon = R.drawable.icon_eye_slash
    ),
    Strict(
        blur = "medium",
        title = R.string.strict_filtering,
        description = R.string.hides_all_content_that_includes_immodest_clothing_or_behavior,
        icon = R.drawable.slash
    ),
    ShowAll(
        blur = "low",
        title = R.string.show_all_content,
        description = R.string.no_filtering_all_images_and_posters_will_be_displayed,
        icon = R.drawable.icon_eye
    );
}