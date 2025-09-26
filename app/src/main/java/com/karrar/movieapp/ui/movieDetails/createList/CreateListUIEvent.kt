package com.karrar.movieapp.ui.movieDetails.createList

sealed interface CreateListUIEvent {
    object ListCreated : CreateListUIEvent
    object Dismiss : CreateListUIEvent
    data class Error(val message: String) : CreateListUIEvent
}
