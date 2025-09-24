package com.karrar.movieapp.domain.enums

import androidx.annotation.StringRes
import com.karrar.movieapp.R

enum class HomeItemsType(@StringRes val stringRes: Int) {
    TOP_RATED_TV_SHOWS(R.string.top_rated_tv_shows),
    RECENTLY_RELEASED(R.string.recently_released),
    UPCOMING(R.string.upcoming_movies),
    RECENTLY_VIEWED(R.string.you_recently_viewed),
    YOUR_COLLECTIONS(R.string.your_collections),
    LATE_NIGHT_THRILLS(R.string.late_night_thrills),
    MIND_BENDING_STORIES(R.string.mind_bending_stories),
    CINEMATIC_MASTERPIECES(R.string.cinematic_masterpieces),
    FAMILY_NIGHT_PICKS(R.string.family_night_picks),
    BASED_ON_TRUE_EVENTS(R.string.based_on_true_events),
    FEEL_GOOD_FAVORITES(R.string.feel_good_favorites),
    MATCHES_YOUR_VIBE(R.string.matches_your_vibe),
    NON(R.string.unknown_section)
}