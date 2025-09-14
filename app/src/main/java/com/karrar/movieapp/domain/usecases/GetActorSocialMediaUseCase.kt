package com.karrar.movieapp.domain.usecases

import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.domain.mappers.MovieMappersContainer
import com.karrar.movieapp.domain.models.ActorSocial
import javax.inject.Inject

class GetActorSocialMediaUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val movieMappersContainer: MovieMappersContainer,
) {
    suspend operator fun invoke(actorId: Int): ActorSocial {
        val response =
            movieRepository.getActorSocialMedia(actorId = actorId)
        return if (response != null) {
            movieMappersContainer.actorSocialMediaMapper.map(response)
        } else {
            throw Throwable("Not Success")
        }
    }
}