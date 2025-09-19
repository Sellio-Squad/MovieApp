package com.karrar.movieapp.ui.youtubePlayer

import com.karrar.movieapp.ui.explore.exploreUIState.ErrorUIState

data class TrailerUIState(
    val videoKey: String = "",
    val isLoading: Boolean = false,
    val error: List<ErrorUIState> = emptyList()
)