package com.karrar.movieapp.ui.search

import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.map
import com.karrar.movieapp.domain.usecases.searchUseCase.*
import com.karrar.movieapp.ui.allMedia.Error
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.ui.search.adapters.ActorSearchInteractionListener
import com.karrar.movieapp.ui.search.adapters.MediaSearchInteractionListener
import com.karrar.movieapp.ui.search.adapters.SearchHistoryInteractionListener
import com.karrar.movieapp.ui.search.mediaSearchUIState.MediaSearchUIState
import com.karrar.movieapp.ui.search.mediaSearchUIState.MediaTypes
import com.karrar.movieapp.ui.search.mediaSearchUIState.MediaUIState
import com.karrar.movieapp.ui.search.mediaSearchUIState.ViewMode
import com.karrar.movieapp.ui.search.mediaSearchUIState.SearchDisplayMode
import com.karrar.movieapp.ui.search.uiStatMapper.SearchHistoryUIStateMapper
import com.karrar.movieapp.ui.search.uiStatMapper.SearchMediaUIStateMapper
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchHistoryUIStateMapper: SearchHistoryUIStateMapper,
    private val searchMediaUIStateMapper: SearchMediaUIStateMapper,
    private val getSearchForMovieUseCase: GetSearchForMovieUseCase,
    private val getSearchForSeriesUserCase: GetSearchForSeriesUserCase,
    private val getSearchForActorUseCase: GetSearchForActorUseCase,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val postSaveSearchResultUseCase: PostSaveSearchResultUseCase,
    private val clearAllSearchHistoryUseCase: ClearAllSearchHistoryUseCase,
) : BaseViewModel(), MediaSearchInteractionListener, ActorSearchInteractionListener,
    SearchHistoryInteractionListener {

    private val _uiState = MutableStateFlow(MediaSearchUIState())
    val uiState = _uiState.asStateFlow()

    private val _searchUIEvent = MutableStateFlow<Event<SearchUIEvent?>>(Event(null))
    val searchUIEvent = _searchUIEvent.asStateFlow()

    init {
        getAllSearchHistory()
    }

    override fun getData() {
        _searchUIEvent.update { Event(SearchUIEvent.ClickRetryEvent) }
    }

    private fun getAllSearchHistory() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                getSearchHistoryUseCase().collect { list ->
                    _uiState.update {
                        it.copy(
                            searchHistory = list.map { item ->
                                searchHistoryUIStateMapper.map(item)
                            },
                            isLoading = false,
                            isEmpty = false
                        )
                    }
                }
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(error = listOf(Error(0, e.message.toString())))
                }
            }
        }
    }

    fun onSearchInputChange(searchTerm: CharSequence) {
        val newSearchTerm = searchTerm.toString()

        // Update the search input immediately for UI responsiveness
        _uiState.update {
            it.copy(searchInput = newSearchTerm)
        }

        // If search term is blank, show suggestions
        if (newSearchTerm.isBlank()) {
            _uiState.update {
                it.copy(displayMode = SearchDisplayMode.SUGGESTIONS)
            }
            return
        }

        // If search term is not blank, show results and perform search
        if (newSearchTerm != _uiState.value.searchInput || _uiState.value.displayMode != SearchDisplayMode.RESULTS) {
            _uiState.update {
                it.copy(
                    displayMode = SearchDisplayMode.RESULTS,
                    isLoading = true
                )
            }

            viewModelScope.launch {
                performSearch(newSearchTerm)
            }
        }
    }

    private suspend fun performSearch(searchTerm: String) {
        when (_uiState.value.searchTypes) {
            MediaTypes.MOVIE -> performMovieSearch(searchTerm)
            MediaTypes.TVS_SHOW -> performSeriesSearch(searchTerm)
            MediaTypes.ACTOR -> performActorSearch(searchTerm)
        }
    }

    private suspend fun performMovieSearch(searchTerm: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                searchResult = getSearchForMovieUseCase(searchTerm).map { pagingData ->
                    pagingData.map { item -> searchMediaUIStateMapper.map(item) }
                }
            )
        }
    }

    private suspend fun performSeriesSearch(searchTerm: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                searchResult = getSearchForSeriesUserCase(searchTerm).map { pagingData ->
                    pagingData.map { item -> searchMediaUIStateMapper.map(item) }
                }
            )
        }
    }

    private suspend fun performActorSearch(searchTerm: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                searchResult = getSearchForActorUseCase(searchTerm).map { pagingData ->
                    pagingData.map { item -> searchMediaUIStateMapper.map(item) }
                }
            )
        }
    }

    fun setViewMode(viewMode: ViewMode) {
        _uiState.update { it.copy(viewMode = viewMode) }
    }

    fun onSearchForMovie() {
        _uiState.update { it.copy(searchTypes = MediaTypes.MOVIE) }

        // If we have a search term and we're in results mode, perform the search
        if (_uiState.value.searchInput.isNotBlank() && _uiState.value.displayMode == SearchDisplayMode.RESULTS) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                performMovieSearch(_uiState.value.searchInput)
            }
        }
    }

    fun onSearchForSeries() {
        _uiState.update { it.copy(searchTypes = MediaTypes.TVS_SHOW) }

        // If we have a search term and we're in results mode, perform the search
        if (_uiState.value.searchInput.isNotBlank() && _uiState.value.displayMode == SearchDisplayMode.RESULTS) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                performSeriesSearch(_uiState.value.searchInput)
            }
        }
    }

    fun onSearchForActor() {
        _uiState.update { it.copy(searchTypes = MediaTypes.ACTOR) }

        // If we have a search term and we're in results mode, perform the search
        if (_uiState.value.searchInput.isNotBlank() && _uiState.value.displayMode == SearchDisplayMode.RESULTS) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                performActorSearch(_uiState.value.searchInput)
            }
        }
    }

    fun onClearAllSearchHistory() {
        viewModelScope.launch {
            clearAllSearchHistoryUseCase()
        }
    }

    override fun onClickActorResult(personID: Int, name: String) {
        saveSearchResult(personID, name)
        _searchUIEvent.update { Event(SearchUIEvent.ClickActorEvent(personID)) }
    }

    override fun onClickMediaResult(media: MediaUIState) {
        saveSearchResult(media.mediaID, media.mediaName)
        _searchUIEvent.update { Event(SearchUIEvent.ClickMediaEvent(media)) }
    }

    private fun saveSearchResult(id: Int, name: String) {
        viewModelScope.launch { postSaveSearchResultUseCase(id, name) }
    }

    override fun onClickSearchHistory(name: String) {
        // When clicking on search history, update the input and trigger search
        _uiState.update {
            it.copy(
                searchInput = name,
                displayMode = SearchDisplayMode.RESULTS,
                isLoading = true
            )
        }

        viewModelScope.launch {
            performSearch(name)
        }
    }

    fun onClickBack() {
        _searchUIEvent.update { Event(SearchUIEvent.ClickBackEvent) }
    }

    fun setErrorUiState(combinedLoadStates: CombinedLoadStates, itemCount: Int) {
        when (combinedLoadStates.refresh) {
            is LoadState.Loading -> {
                _uiState.update {
                    it.copy(isLoading = true, error = emptyList(), isEmpty = false)
                }
            }
            is LoadState.Error -> {
                _uiState.update {
                    it.copy(isLoading = false, error = listOf(Error(404, "")), isEmpty = false)
                }
            }
            is LoadState.NotLoading -> {
                if (itemCount < 1) {
                    _uiState.update {
                        it.copy(
                            isEmpty = true,
                            isLoading = false,
                            error = emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isEmpty = false,
                            isLoading = false,
                            error = emptyList()
                        )
                    }
                }
            }
        }
    }

    override fun onSuggestionClick(query: String) {
        // When clicking on a suggestion, treat it like typing in the search box
        onSearchInputChange(query)
    }

    override fun onMediaClick(media: MediaUIState) {
        saveSearchResult(media.mediaID, media.mediaName)
        _searchUIEvent.update { Event(SearchUIEvent.ClickMediaEvent(media)) }
    }
}