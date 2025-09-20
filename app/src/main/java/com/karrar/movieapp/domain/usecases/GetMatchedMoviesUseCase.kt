package com.karrar.movieapp.domain.usecases

import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.domain.ResultHandler
import com.karrar.movieapp.domain.models.MovieDetails
import com.karrar.movieapp.domain.usecases.movieDetails.GetMovieDetailsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMatchedMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val movieDetailsUseCase: GetMovieDetailsUseCase

) {
    suspend operator fun invoke(
        page: Int,
        genres: String?,
        runtimeGte: Int?,
        runtimeLte: Int?,
        releaseDateGte: String?,
        releaseDateLte: String?
    ): Flow<List<MovieDetails>> {

        return movieRepository.getMatchedMovies(
            page = page,
            genres = genres,
            runtimeGte = runtimeGte,
            runtimeLte = runtimeLte,
            releaseDateGte = releaseDateGte,
            releaseDateLte = releaseDateLte
        ).map {
            it.map { movie ->
                val result = movieDetailsUseCase.getMovieDetails(movie.id)
                when (result) {
                    is ResultHandler.Success -> result.data
                    is ResultHandler.Error -> throw result.throwable
                }
            }

        }
    }
}