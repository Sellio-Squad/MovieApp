package com.karrar.movieapp.domain.models

import com.karrar.movieapp.domain.enums.MediaType
import com.karrar.movieapp.ui.movieDetails.movieDetailsUIState.ErrorUIState

data class TvShowDetails(
    val tvShowId: Int = 0,
    val tvShowImage: String = "",
    val tvShowName: String = "",
    val tvShowReleaseDate: String = "",
    val tvShowGenres: String = "",
    val tvShowSeasonsNumber: Int = 0,
    val tvShowReview: Int = 0,
    val tvShowVoteAverage: String = "",
    val tvShowOverview: String = "",
    val tvShowSeasons: List<Season> = emptyList(),
    val tvShowDuration: Int = 0,
    val tvShowType: MediaType = MediaType.TV_SHOW,
    val isLoading: Boolean = false,
    val errorUIStates: List<ErrorUIState> = emptyList()
)