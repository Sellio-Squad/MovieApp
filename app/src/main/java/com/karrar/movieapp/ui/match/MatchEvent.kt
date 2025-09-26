package com.karrar.movieapp.ui.match

sealed class MatchEvent {
    data class OnMovieClick(val id: Int) : MatchEvent()
    data class OnSaveClick(val id: Int) : MatchEvent()
    data class OnPlayTrailerClick(val id: Int) : MatchEvent()
}