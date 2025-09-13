package com.karrar.movieapp.domain.usecases.searchUseCase

import com.karrar.movieapp.data.repository.MovieRepository
import javax.inject.Inject

class ClearAllSearchHistoryUseCase @Inject constructor(
    private val movieRepository: MovieRepository
    ) {
    suspend operator fun invoke() {
        movieRepository.clearAllRecentSearch()
    }
}