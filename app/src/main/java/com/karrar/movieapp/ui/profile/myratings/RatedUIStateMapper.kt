package com.karrar.movieapp.ui.profile.myratings

import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.Genre
import com.karrar.movieapp.domain.models.Rated
import javax.inject.Inject

class RatedUIStateMapper @Inject constructor() :

    Mapper<Rated, RatedUIState> {
    override fun map(input: Rated): RatedUIState {
        return RatedUIState(
            id = input.id,
            title = input.title,
            posterPath = input.posterPath,
            rating = input.rating,
            mediaType = input.mediaType,
            releaseDate = input.releaseDate,
        )
    }

    fun map(input: Rated, genres: List<Genre>): RatedUIState {
        return RatedUIState(
            id = input.id,
            title = input.title,
            posterPath = input.posterPath,
            rating = input.rating,
            mediaType = input.mediaType,
            releaseDate = input.releaseDate,
            genres = genres.filter { genre ->
                input.genres.contains(genre.genreID)
            }.joinToString(",") { it.genreName }
        )
    }
}