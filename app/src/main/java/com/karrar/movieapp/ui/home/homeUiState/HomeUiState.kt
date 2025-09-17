package com.karrar.movieapp.ui.home.homeUiState

import com.karrar.movieapp.ui.home.HomeFeaturedCollections
import com.karrar.movieapp.ui.home.HomeItem
import com.karrar.movieapp.ui.home.model.FeaturedCollectionsItem

data class HomeUiState (
    val popularMovies: HomeItem = HomeItem.Slider(emptyList()),
    val recentlyReleasedMovies: HomeItem = HomeItem.RecentlyReleased(emptyList()),
    val upcomingMovies: HomeItem = HomeItem.Upcoming(emptyList()),
    val matchesYourVibes: HomeItem = HomeItem.MatchesYourVibes(emptyList()),
    val onTheAiringSeries: HomeItem = HomeItem.OnTheAiring(emptyList()),
    val browseEverything: HomeItem = HomeItem.BrowseEverything(),
    val letUsChooseForYou: HomeItem = HomeItem.LetUsChooseForYou(),
    val featuredCollections: HomeItem = HomeItem.FeaturedCollections(
        items = HomeFeaturedCollections.values().map { collections ->
            FeaturedCollectionsItem(
                title = collections.title,
                image = collections.image,
                genreName = collections.genreName,
                genreId = collections.genreId,
                type = collections.type
            )
        }
    ),
    val recentlyViewed: HomeItem = HomeItem.RecentlyViewed(emptyList()),
    val collections: HomeItem = HomeItem.CollectionsList(emptyList()),
    val isLoading:Boolean = false,
    val error : List<String> = emptyList(),
    val username: String? = null,
    val isLoggedIn: Boolean = false
)