package com.karrar.movieapp.ui.myCollection.myCollectionUIState

import com.karrar.movieapp.ui.category.uiState.ErrorUIState

data class MyListUIState(
    val createdList: List<CreatedListUIState> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: List<ErrorUIState> = emptyList()
)