package com.karrar.movieapp.ui.galleryActor

import com.karrar.movieapp.domain.mappers.Mapper
import javax.inject.Inject

class GalleryUrlUIMapper @Inject constructor() : Mapper<String, GalleryUrlUIState> {
    override fun map(input: String): GalleryUrlUIState {
        return GalleryUrlUIState(
            galleryUrl = input
        )
    }

}