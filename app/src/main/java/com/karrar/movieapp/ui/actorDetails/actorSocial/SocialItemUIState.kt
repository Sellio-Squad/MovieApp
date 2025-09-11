package com.karrar.movieapp.ui.actorDetails.actorSocial

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import com.karrar.movieapp.R

data class SocialItemUIState(
    @SuppressLint("ResourceType") @StringRes val iconRes: Int = R.drawable.colored_facebook,
    val label: String = "",
    val url: String = ""
)
