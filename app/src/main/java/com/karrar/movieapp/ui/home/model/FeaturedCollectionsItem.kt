package com.karrar.movieapp.ui.home.model

import androidx.annotation.DrawableRes
import com.karrar.movieapp.domain.enums.HomeItemsType

data class FeaturedCollectionsItem(
    val title: String,
    @DrawableRes val image: Int,
    val genreName: String,
    val genreId: Int,
    val type: HomeItemsType
)