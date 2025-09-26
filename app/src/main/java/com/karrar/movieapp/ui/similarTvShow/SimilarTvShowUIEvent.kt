package com.karrar.movieapp.ui.similarTvShow

sealed class SimilarTvShowUIEvent {
    object ClickBackEvent : SimilarTvShowUIEvent()
    data class ClickTvShowEvent(val tvShowID: Int) : SimilarTvShowUIEvent()
}