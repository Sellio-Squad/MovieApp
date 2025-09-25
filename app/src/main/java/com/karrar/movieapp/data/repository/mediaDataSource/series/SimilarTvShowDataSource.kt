package com.karrar.movieapp.data.repository.mediaDataSource.series

import androidx.paging.PagingState
import com.karrar.movieapp.data.remote.response.TVShowsDTO
import com.karrar.movieapp.data.remote.service.MovieService
import com.karrar.movieapp.data.repository.mediaDataSource.BasePagingSource
import javax.inject.Inject


class SimilarTvShowDataSource @Inject constructor(
    private val service: MovieService,
    private val tvShowId: Int
) : BasePagingSource<TVShowsDTO>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TVShowsDTO> {
        val pageNumber = params.key ?: 1

        return try {
            val response = service.getSimilarTvShows(
                page = pageNumber,
                tvShowId = tvShowId
            )

            LoadResult.Page(
                data = response.body()?.items ?: emptyList(),
                prevKey = null,
                nextKey = response.body()?.page?.plus(1)
            )
        } catch (e: Throwable) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TVShowsDTO>): Int? {
        return state.anchorPosition
    }
}