package com.karrar.movieapp.ui.myList.myListUIState

import com.karrar.movieapp.ui.explore.exploreUIState.ErrorUIState

data class CreateListDialogUIState(
    val mediaListName: String = "",
    val error: List<ErrorUIState> = emptyList()
)