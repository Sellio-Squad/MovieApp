package com.karrar.movieapp.domain.usecases

import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.domain.mappers.movie.MatchVibesMovieMovieMapper
import com.karrar.movieapp.domain.models.Media
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMatchedMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val movieMapper: MatchVibesMovieMovieMapper
) {
    suspend operator fun invoke(
        page: Int,
        genres: String?,
        runtimeGte: Int?,
        runtimeLte: Int?,
        releaseDateGte: String?,
        releaseDateLte: String?
    ): Flow<List<Media>> {
        return movieRepository.getMatchedMovies(
                page = page,
                genres = genres,
                runtimeGte = runtimeGte,
                runtimeLte = runtimeLte,
                releaseDateGte = releaseDateGte,
                releaseDateLte = releaseDateLte
        ).map { it.map(movieMapper::map) }
    }
}