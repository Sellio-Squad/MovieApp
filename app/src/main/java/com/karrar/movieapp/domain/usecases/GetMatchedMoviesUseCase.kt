package com.karrar.movieapp.domain.usecases

import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.domain.mappers.movie.MovieMapper
import com.karrar.movieapp.domain.models.Media
import javax.inject.Inject

class GetMatchedMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val movieMapper: MovieMapper
) {
    suspend operator fun invoke(
        page: Int,
        genres: String?,
        runtimeGte: Int?,
        runtimeLte: Int?,
        releaseDateGte: String?,
        releaseDateLte: String?
    ): List<Media> {
        return try {
            val response = movieRepository.getMatchedMovies(
                page = page,
                genres = genres,
                runtimeGte = runtimeGte,
                runtimeLte = runtimeLte,
                releaseDateGte = releaseDateGte,
                releaseDateLte = releaseDateLte
            )
            response?.items?.map { movieMapper.map(it) } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}