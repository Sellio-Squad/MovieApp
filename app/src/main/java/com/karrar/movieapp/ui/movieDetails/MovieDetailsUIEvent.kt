package com.karrar.movieapp.ui.movieDetails

import com.karrar.movieapp.domain.enums.AllMediaType

sealed interface MovieDetailsUIEvent {
    object ClickBackEvent : MovieDetailsUIEvent
    object ClickPlayTrailerEvent : MovieDetailsUIEvent
    object ClickSaveEvent : MovieDetailsUIEvent
    object MessageAppear : MovieDetailsUIEvent
    object ShowLoginDialogEvent : MovieDetailsUIEvent
    object ClickReviewsEvent : MovieDetailsUIEvent
    data class ClickSeeAllMovieEvent(val mediaType: AllMediaType) : MovieDetailsUIEvent
    data class ClickMovieEvent(val movieID: Int) : MovieDetailsUIEvent
    data class ClickCastEvent(val castID: Int) : MovieDetailsUIEvent

}