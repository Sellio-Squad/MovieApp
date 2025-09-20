package com.karrar.movieapp.ui.search.mediaSearchUIState

enum class MediaTypes {
    MOVIE,
    TVS_SHOW,
    ACTOR;

    override fun toString(): String {
        return super.toString().lowercase()
    }
}