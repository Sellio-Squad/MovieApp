package com.karrar.movieapp.ui.myCollection.collectionDetails.collectionDetailsUIState

import com.karrar.movieapp.ui.category.uiState.ErrorUIState


data class CollectionDetailsUIState(
    val savedMedia: List<SavedMediaUIState> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: List<ErrorUIState> = emptyList()
)