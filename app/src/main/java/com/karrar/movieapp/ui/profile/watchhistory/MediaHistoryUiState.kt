package com.karrar.movieapp.ui.profile.watchhistory

data class MediaHistoryUiState(
    val id: Int,
    var posterPath: String,
    var movieTitle: String,
    var voteAverage: String,
    var releaseDate: String,
    var movieDuration: String,
    var mediaType: String,
    val genres: String,
)