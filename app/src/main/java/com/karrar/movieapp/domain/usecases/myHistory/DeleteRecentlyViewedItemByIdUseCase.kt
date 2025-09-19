package com.karrar.movieapp.domain.usecases.myHistory

import com.karrar.movieapp.data.repository.MovieRepository
import javax.inject.Inject

class DeleteRecentlyViewedItemByIdUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    suspend operator fun invoke(
        id: Int
    ) = movieRepository.deleteRecentlyViewedItemById(id = id)

}