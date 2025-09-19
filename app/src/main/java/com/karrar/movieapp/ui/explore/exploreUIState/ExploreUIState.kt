package com.karrar.movieapp.ui.explore.exploreUIState

import androidx.paging.PagingData
import com.karrar.movieapp.utilities.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow


data class ExploreUIState(
    val genres: List<GenreUIState> = emptyList(),
    val selectedGenreID: Int = Constants.FIRST_CATEGORY_ID,
    val media: Flow<PagingData<MediaUIState>> = emptyFlow(),
    val currentType: String = Constants.MOVIE,
    val isLoading: Boolean = false,
    val error: List<ErrorUIState> = emptyList(),
    val viewMode: ViewMode = ViewMode.GRID,
    val selectedTab: Int = 0
    )

enum class ViewMode {
    LIST,
    GRID
}