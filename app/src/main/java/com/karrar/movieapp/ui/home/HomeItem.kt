package com.karrar.movieapp.ui.home

import com.karrar.movieapp.domain.enums.HomeItemsType
import com.karrar.movieapp.ui.models.ActorUiState
import com.karrar.movieapp.ui.home.homeUiState.PopularUiState
import com.karrar.movieapp.ui.models.MediaUiState

sealed class HomeItem(val priority: Int) {

    data class Slider(val items: List<PopularUiState>) : HomeItem(0)

    data class RecentlyReleased(val items: List<MediaUiState>, val type: HomeItemsType = HomeItemsType.RECENTLY_RELEASED) : HomeItem(1)

    data class Upcoming(val items: List<MediaUiState>, val type: HomeItemsType = HomeItemsType.UPCOMING) : HomeItem(2)

    data class MatchesYourVibes(val items: List<MediaUiState>, val type: HomeItemsType = HomeItemsType.MATCHES_YOUR_VIBE) : HomeItem(3)

    data class Trending(val items: List<MediaUiState>, val type: HomeItemsType = HomeItemsType.TRENDING) : HomeItem(4)

    data class AiringToday(val items: List<MediaUiState>) : HomeItem(5)

    data class OnTheAiring(val items: List<MediaUiState>, val type: HomeItemsType = HomeItemsType.ON_THE_AIR) : HomeItem(6)

    data class TvShows(val items: List<MediaUiState>) : HomeItem(7)

    data class Mystery(val items: List<MediaUiState>, val type: HomeItemsType = HomeItemsType.MYSTERY) : HomeItem(8)

    data class Adventure(val items: List<MediaUiState>, val type: HomeItemsType = HomeItemsType.ADVENTURE) : HomeItem(9)

    data class Actor(val items: List<ActorUiState>) : HomeItem(10)

}