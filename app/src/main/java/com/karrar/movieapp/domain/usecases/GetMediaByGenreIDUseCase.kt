package com.karrar.movieapp.domain.usecases

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.data.repository.SeriesRepository
import com.karrar.movieapp.domain.mappers.movie.MovieMapper
import com.karrar.movieapp.domain.mappers.movie.SearchMovieMapper
import com.karrar.movieapp.domain.mappers.search.SearchSeriesMapper
import com.karrar.movieapp.domain.mappers.series.TVShowMapper
import com.karrar.movieapp.domain.models.Media
import com.karrar.movieapp.domain.models.SearchMedia
import com.karrar.movieapp.utilities.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMediaByGenreIDUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository,
    private val searchMovieMapper: SearchMovieMapper,
    private val searchSeriesMapper: SearchSeriesMapper
) {

    suspend operator fun invoke(mediaType: Int, genreID: Int): Flow<PagingData<SearchMedia>> {
        return when (mediaType) {
            Constants.MOVIE_CATEGORIES_ID -> {
                getMovies(genreID)
            }
            else -> {
                getTVShows(genreID)
            }
        }
    }

    private suspend fun getMovies(genreID: Int): Flow<PagingData<SearchMedia>> {
        return if (genreID == Constants.FIRST_CATEGORY_ID) {
            wrapper(movieRepository::getAllMovies, searchMovieMapper::map)
        } else {
            wrapper({ movieRepository.getMovieByGenre(genreID) }, searchMovieMapper::map)
        }
    }

    private suspend fun getTVShows(genreID: Int): Flow<PagingData<SearchMedia>> {
        return if (genreID == Constants.FIRST_CATEGORY_ID) {
            wrapper(seriesRepository::getAllTVShows, searchSeriesMapper::map)

        } else {
            wrapper({ seriesRepository.getTVShowByGenre(genreID) }, searchSeriesMapper::map)
        }
    }

    private suspend fun <T : Any> wrapper(
        data: suspend () -> Pager<Int, T>,
        mapper: (T) -> SearchMedia,
    ): Flow<PagingData<SearchMedia>> {
        return data().flow.map { pager -> pager.map { mapper(it) } }
    }
}