package com.karrar.movieapp.domain.models

data class SaveListDetails(
    val id: Int = 0,
    val mediaType: String = "",
    val title: String = "",
    val releaseDate: String = "",
    val voteAverage: Double = 0.0,
    val posterPath: String = "",
    val duration: String = "",
    val genres: List<String>? = null
)
