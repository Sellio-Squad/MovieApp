package com.karrar.movieapp.ui.tvShowDetails

import com.karrar.movieapp.domain.enums.AllMediaType
import com.karrar.movieapp.domain.enums.TvShowItemsType


sealed interface TvShowDetailsUIEvent {
    object ClickBackEvent : TvShowDetailsUIEvent
    object ClickPlayTrailerEvent : TvShowDetailsUIEvent
    object MessageAppear : TvShowDetailsUIEvent
    object ClickReviewsEvent : TvShowDetailsUIEvent
    data class ClickSeasonEvent(val seasonId: Int) : TvShowDetailsUIEvent
    data class ClickCastEvent(val castID: Int) : TvShowDetailsUIEvent
    data class ClickTvShowEvent(val tvShowID: Int ) : TvShowDetailsUIEvent
    data class ClickSeeAllTvShowsEvent(val mediaType: TvShowItemsType) : TvShowDetailsUIEvent

    data class ClickShowMoreSeasons(val tvShowId: Int) : TvShowDetailsUIEvent
}