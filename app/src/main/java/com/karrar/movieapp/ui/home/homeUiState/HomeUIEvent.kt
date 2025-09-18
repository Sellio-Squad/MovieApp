package com.karrar.movieapp.ui.home.homeUiState

import com.karrar.movieapp.domain.enums.AllMediaType
import com.karrar.movieapp.ui.myCollection.myCollectionUIState.CreatedListUIState

sealed interface HomeUIEvent {
    data class ClickMovieEvent(val movieID: Int) : HomeUIEvent
    data class ClickSeriesEvent(val seriesID: Int) : HomeUIEvent
    data class ClickSeeAllMovieEvent(val mediaType: AllMediaType) : HomeUIEvent
    data class ClickFeaturedCollectionsEvent(val mediaType: AllMediaType) : HomeUIEvent
    data class ClickSeeAllTVShowsEvent(val mediaType: AllMediaType) : HomeUIEvent
    object ClickSeeAllRecentlyViewedEvent: HomeUIEvent
    object ClickBrowseEverythingEvent : HomeUIEvent
    object ClickLetUsChooseForYouEvent : HomeUIEvent
    data class ClickCollectionList(val list: CreatedListUIState): HomeUIEvent
    object ClickSeeAllCollectionsEvent: HomeUIEvent
}