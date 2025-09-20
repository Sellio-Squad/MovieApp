package com.karrar.movieapp.ui.search.mediaSearchUIState

import androidx.paging.PagingData
import com.karrar.movieapp.ui.allMedia.Error
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class MediaSearchUIState(
    val searchInput: String = "",
    val searchTypes: MediaTypes = MediaTypes.MOVIE,
    val searchResult: Flow<PagingData<MediaUIState>> = emptyFlow(),
    val searchHistory: List<SearchHistoryUIState> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: List<Error> = emptyList(),
    val viewMode: ViewMode = ViewMode.GRID,
    val displayMode: SearchDisplayMode = SearchDisplayMode.SUGGESTIONS,
    val hasValidResults: Boolean = false,
    val lastSearchTerm: String = "",
    val lastSearchType: MediaTypes = MediaTypes.MOVIE,
    val lastViewMode: ViewMode = ViewMode.GRID
)

enum class SearchDisplayMode {
    SUGGESTIONS,
    RESULTS
}

enum class ViewMode {
    LIST,
    GRID
}
