package com.karrar.movieapp.ui.tvShowDetails.tvShowUIMapper

import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.TvShowDetails
import com.karrar.movieapp.ui.tvShowDetails.tvShowUIState.TvShowDetailsResultUIState
import javax.inject.Inject

data class TvShowDuration(val hours: Int, val minutes: Int)

class TvShowDetailsResultUIMapper @Inject constructor() : Mapper<TvShowDetails, TvShowDetailsResultUIState> {
    override fun map(input: TvShowDetails): TvShowDetailsResultUIState {
        val duration = formatMovieDuration(input.tvShowDuration)
        return TvShowDetailsResultUIState(
            tvShowId = input.tvShowId,
            tvShowName = input.tvShowName,
            tvShowOverview = input.tvShowOverview,
            tvShowImage = input.tvShowImage,
            tvShowVoteAverage = input.tvShowVoteAverage,
            tvShowReview = input.tvShowReview,
            tvShowReleaseDate = input.tvShowReleaseDate,
            tvShowGenres = input.tvShowGenres,
            tvShowSeasonsNumber = input.tvShowSeasonsNumber,
            tvShowMinutes = duration.minutes,
            tvShowHours = duration.hours

        )
    }
    private fun formatMovieDuration(duration: Int): TvShowDuration {
        return TvShowDuration(hours = duration.div(60), minutes = duration.rem(60))
    }
}
