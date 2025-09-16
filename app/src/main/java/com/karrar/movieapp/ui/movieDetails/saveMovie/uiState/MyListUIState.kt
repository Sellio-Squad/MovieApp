package com.karrar.movieapp.ui.movieDetails.saveMovie.uiState

import com.karrar.movieapp.ui.explore.exploreUIState.ErrorUIState

data class MySavedListUIState(
    val myListItemUI: List<MyListItemUI> = emptyList(),
    val isLoading: Boolean = false,
    val error: List<ErrorUIState> = emptyList()
)

