package com.karrar.movieapp.ui.myCollection.myCollectionUIState

sealed interface MyListUIEvent {
    object CreateButtonClicked : MyListUIEvent
    object StartCollectingButtonClicked : MyListUIEvent
    object CLickAddEvent : MyListUIEvent
    data class OnSelectItem(val createdListUIState: CreatedListUIState) : MyListUIEvent
    data class DisplayError(val errorMessage: String) : MyListUIEvent
}