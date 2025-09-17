package com.karrar.movieapp.ui.actorDetails

import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.ActorGallery
import javax.inject.Inject

class ActorGalleriesUIMapper @Inject constructor() : Mapper<ActorGallery, ActorGalleryUIState> {
    override fun map(input: ActorGallery): ActorGalleryUIState {
        return ActorGalleryUIState(
            id = input.id,
            galleryUrl = input.galleryUrl
        )
    }
}
