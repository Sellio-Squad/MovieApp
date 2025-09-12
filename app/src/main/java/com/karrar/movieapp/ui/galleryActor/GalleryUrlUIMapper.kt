package com.karrar.movieapp.ui.galleryActor

import com.karrar.movieapp.domain.mappers.Mapper
import javax.inject.Inject

class GalleryUrlUIMapper @Inject constructor() : Mapper<List<String>, GalleryUrlUIState> {
    override fun map(input: List<String>): GalleryUrlUIState {
        return GalleryUrlUIState(
            images = input
        )
    }

}