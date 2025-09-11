package com.karrar.movieapp.domain.usecase.home.getData.movie

import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.domain.mappers.movie.AdventureMovieMapper
import com.karrar.movieapp.domain.models.Media
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetMatchesYourVibeMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
) {

    suspend operator fun invoke(): Flow<List<Media>> {
        return flowOf(listOf<Media>())
    }
    // TODO: This fake useCase need to return from repo and create mapper
}