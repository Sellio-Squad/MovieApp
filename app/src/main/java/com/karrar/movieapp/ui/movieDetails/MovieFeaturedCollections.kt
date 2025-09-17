package com.karrar.movieapp.ui.movieDetails

import androidx.annotation.DrawableRes
import com.karrar.movieapp.R
import com.karrar.movieapp.domain.enums.HomeItemsType

enum class MovieFeaturedCollections(
    val title: String,
    @DrawableRes val image: Int,
    val genreName: String,
    val genreId: Int,
    val type: HomeItemsType
) {
    LATE_NIGHT_THRILLS(
        title = "Late Night Thrills",
        image = R.drawable.late_night_thrills,
        genreName = "horror",
        genreId = 27,
        type = HomeItemsType.LATE_NIGHT_THRILLS
    ),
    MIND_BENDING_STORIES(
        title = "Mind Bending Stories",
        image = R.drawable.mind_bending_stories,
        genreName = "science fiction",
        genreId = 878,
        type = HomeItemsType.MIND_BENDING_STORIES
    ),
    CINEMATIC_MASTERPIECES(
        title = "Cinematic Masterpieces",
        image = R.drawable.cinematic_masterpieces,
        genreName = "drama",
        genreId = 18,
        type = HomeItemsType.CINEMATIC_MASTERPIECES
    ),
    FAMILY_NIGHT_PICKS(
        title = "Family Night Picks",
        image = R.drawable.family_night_picks,
        genreName = "family",
        genreId = 10751,
        type = HomeItemsType.FAMILY_NIGHT_PICKS
    ),
    BASED_ON_TRUE_EVENTS(
        title = "Based On True Events",
        image = R.drawable.based_on_true_events,
        genreName = "history",
        genreId = 36,
        type = HomeItemsType.BASED_ON_TRUE_EVENTS
    ),
    FEEL_GOOD_FAVORITES(
        title = "Feel Good Favorites",
        image = R.drawable.feel_good_favorites,
        genreName = "comedy",
        genreId = 35,
        type = HomeItemsType.FEEL_GOOD_FAVORITES
    )
}