package com.karrar.movieapp.ui.movieDetails.saveMovie.uiState

sealed interface SaveMovieUIEvent {
    data class DisplayMessage(val message: String) : SaveMovieUIEvent
    object NavigateToCreateListDialog : SaveMovieUIEvent
    object DismissSheet : SaveMovieUIEvent
}