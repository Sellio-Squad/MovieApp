package com.karrar.movieapp.domain.usecases

import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.domain.mappers.MovieMappersContainer
import com.karrar.movieapp.domain.models.ActorGallery
import javax.inject.Inject

class GetActorGalleryUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val movieMappersContainer: MovieMappersContainer,
) {
    suspend operator fun invoke(actorId: Int): ActorGallery {
        val response =
            movieRepository.getGalleryActor(actorId = actorId)
        return if (response != null) {
            movieMappersContainer.actorGalleryMapper.map(response)
        } else {
            throw Throwable("Not Success")
        }
    }
}