package com.karrar.movieapp.data.local.mappers.movie

import com.karrar.movieapp.data.local.database.entity.movie.MatchVibesMovieEntity
import com.karrar.movieapp.data.remote.response.MovieDto
import com.karrar.movieapp.domain.mappers.Mapper
import javax.inject.Inject

class MatchVibesMovieMapper @Inject constructor() : Mapper<MovieDto, MatchVibesMovieEntity> {
    override fun map(input: MovieDto): MatchVibesMovieEntity {
        return MatchVibesMovieEntity(
            id = input.id ?: 0,
            title = input.originalTitle ?: "",
            imageUrl = input.posterPath ?: "",
            movieRate = input.voteAverage ?: 0.0,
        )
    }
}