package com.karrar.movieapp.domain.mappers

import com.karrar.movieapp.data.local.database.entity.WatchHistoryEntity
import com.karrar.movieapp.ui.profile.watchhistory.MediaHistoryUiState
import com.karrar.movieapp.utilities.DateFormatter
import javax.inject.Inject

class WatchHistoryMapper @Inject constructor() : Mapper<WatchHistoryEntity, MediaHistoryUiState> {
    override fun map(input: WatchHistoryEntity): MediaHistoryUiState {
        val hours = (input.movieDuration) / 60
        val minutes = (input.movieDuration) % 60
        return MediaHistoryUiState(
            input.id,
            input.posterPath,
            input.movieTitle,
            input.voteAverage,
            DateFormatter.toUiDate(input.releaseDate),
            movieDuration = if (input.mediaType.equals(
                    com.karrar.movieapp.utilities.Constants.MOVIE,
                    true
                )
            ) "${hours}h ${minutes}m" else "${input.movieDuration} Seasons",
            input.mediaType,
            input.genres,
        )
    }
}