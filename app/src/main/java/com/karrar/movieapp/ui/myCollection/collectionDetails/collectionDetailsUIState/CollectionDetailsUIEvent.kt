package com.karrar.movieapp.ui.myCollection.collectionDetails.collectionDetailsUIState

sealed interface CollectionDetailsUIEvent {
    data class OnItemSelected(val savedMediaUIState: SavedMediaUIState) : CollectionDetailsUIEvent
}