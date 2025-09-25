package com.karrar.movieapp.ui.similarTvShow

sealed class SimilarTvShowUIEvent {
    data class ClickTvShowEvent(val tvShowID: Int) : SimilarTvShowUIEvent()
}