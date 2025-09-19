package com.karrar.movieapp.ui.profile.myratings

data class MyRateUIState(
    val isLoading: Boolean = false,
    val ratedList: List<RatedUIState> = emptyList(),
    val currentTypeList: List<RatedUIState> = emptyList(),
    val error: List<Error> = emptyList(),
    val contentType: ContentType = ContentType.MOVIES,
    val showTip: Boolean = false
) {
    enum class ContentType {
        MOVIES, TV_SHOWS
    }

}