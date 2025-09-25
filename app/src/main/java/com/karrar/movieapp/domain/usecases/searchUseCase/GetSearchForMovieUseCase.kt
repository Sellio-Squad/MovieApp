package com.karrar.movieapp.domain.usecases.searchUseCase

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.domain.mappers.movie.SearchMovieMapper
import com.karrar.movieapp.domain.models.SearchMedia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class GetSearchForMovieUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val searchMovieMapper: SearchMovieMapper
    ) {

    suspend operator fun invoke(searchTerm: String):Flow<PagingData<SearchMedia>>{
        return wrapper({movieRepository.searchForMoviePager(searchTerm)}, searchMovieMapper::map)
    }

    private suspend fun <T : Any> wrapper(
        data: suspend () -> Pager<Int, T>,
        mapper: (T) -> SearchMedia,
    ): Flow<PagingData<SearchMedia>> {
        return data().flow.map { pager -> pager.map { mapper(it) } }
    }

}