package com.karrar.movieapp.ui.tvShowDetails.tvShowUIMapper

import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.Media
import com.karrar.movieapp.ui.models.MediaUiState
import javax.inject.Inject

class SimilarTvShowUIStateMapper @Inject constructor() : Mapper<Media, MediaUiState> {

    override fun map(media: Media): MediaUiState {
        return MediaUiState(
            id = media.mediaID,
            imageUrl = media.mediaImage,
            rate = media.mediaRate,
            mediaTitle = media.mediaName,
        )
    }
}