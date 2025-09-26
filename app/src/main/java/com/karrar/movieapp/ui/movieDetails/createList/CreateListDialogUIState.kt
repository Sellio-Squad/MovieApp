package com.karrar.movieapp.ui.movieDetails.createList

import com.karrar.movieapp.ui.movieDetails.movieDetailsUIState.ErrorUIState

data class CreateListDialogUIState(
    val mediaListName: String = "",
    val error: List<ErrorUIState> = emptyList()
)