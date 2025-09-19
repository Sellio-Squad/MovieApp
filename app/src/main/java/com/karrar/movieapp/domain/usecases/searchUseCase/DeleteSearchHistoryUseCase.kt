package com.karrar.movieapp.domain.usecases.searchUseCase

import com.karrar.movieapp.data.local.database.entity.SearchHistoryEntity
import com.karrar.movieapp.data.repository.MovieRepository
import jakarta.inject.Inject

class DeleteSearchHistoryUseCase @Inject constructor (
    private val movieRepository: MovieRepository
    ) {
        suspend operator fun invoke(
            id: Long,
            name: String
        ) {
            movieRepository.deleteSearchItem(
                SearchHistoryEntity(
                    id = id,
                    search = name
                )
            )
        }
}