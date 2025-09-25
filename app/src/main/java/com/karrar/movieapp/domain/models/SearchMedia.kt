package com.karrar.movieapp.domain.models

data class SearchMedia(
    val mediaID: Int,
    val mediaImage: String,
    val mediaType: String,
    val mediaName: String,
    val mediaDate: String,
    val mediaRate: Float,
    val mediaGenre: List<String>
)
