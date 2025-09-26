package com.karrar.movieapp.domain.usecases.searchUseCase

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.domain.mappers.search.SearchActorMapper
import com.karrar.movieapp.domain.models.SearchMedia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class GetSearchForActorUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val searchActorMapper: SearchActorMapper
    ) {

    suspend operator fun invoke(searchTerm: String): Flow<PagingData<SearchMedia>> {
        return wrapper({movieRepository.searchForActorPager(searchTerm)}, searchActorMapper::map)
    }

    private suspend fun <T : Any> wrapper(
        data: suspend () -> Pager<Int, T>,
        mapper: (T) -> SearchMedia,
    ): Flow<PagingData<SearchMedia>> {
        return data().flow.map { pager -> pager.map { mapper(it) } }
    }

}