package com.karrar.movieapp.ui.explore.exploreUIState

import com.karrar.movieapp.utilities.Constants

data class GenreUIState(
    val genreID: Int = Constants.FIRST_CATEGORY_ID,
    val genreName: String = ""
)