package com.karrar.movieapp.ui.explore.exploreUIState

data class MediaUIState(
    val mediaID: Int,
    val mediaImage: String,
    val mediaType: String,
    val mediaName: String,
    val mediaVoteAverage: Float,
    val mediaReleaseDate: String,
)