package com.karrar.movieapp.domain.mappers.actor

import com.karrar.movieapp.BuildConfig
import com.karrar.movieapp.data.remote.response.actor.ActorGalleryDto
import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.ActorGallery
import javax.inject.Inject

class ActorGalleryMapper @Inject constructor() : Mapper<ActorGalleryDto, ActorGallery> {
    override fun map(input: ActorGalleryDto): ActorGallery {
        return ActorGallery(
            input.id ?: 0,
            input.profileDto?.map { BuildConfig.IMAGE_BASE_PATH + it.filePath } ?: emptyList()
        )
    }
}