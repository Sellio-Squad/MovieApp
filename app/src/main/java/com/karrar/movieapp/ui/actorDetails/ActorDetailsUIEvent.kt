package com.karrar.movieapp.ui.actorDetails

sealed interface ActorDetailsUIEvent {
    object BackEvent : ActorDetailsUIEvent
    object SeeAllMovies : ActorDetailsUIEvent
    object SeeAllGallery: ActorDetailsUIEvent
    data class ClickMovieEvent(val movieID: Int) : ActorDetailsUIEvent
}