package com.karrar.movieapp.domain.usecases.home.getData.movie

import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.domain.mappers.movie.MatchVibesMovieToMediaMapper
import com.karrar.movieapp.domain.models.Media
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMatchesYourVibeMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val movieMapper: MatchVibesMovieToMediaMapper
) {

    suspend operator fun invoke(): Flow<List<Media>> {
        return movieRepository.getCurrentMatchVibesMovies()
            .map { it.map(movieMapper::map) }
    }
}