package com.karrar.movieapp.ui.search

import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
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
import kotlinx.coroutines.flow.Flow
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
    private val deleteSearchHistoryUseCase: DeleteSearchHistoryUseCase
) : BaseViewModel(), MediaSearchInteractionListener, ActorSearchInteractionListener,
    SearchHistoryInteractionListener {

    private val _uiState = MutableStateFlow(MediaSearchUIState())
    val uiState = _uiState.asStateFlow()

    private val _searchUIEvent = MutableStateFlow<Event<SearchUIEvent?>>(Event(null))
    val searchUIEvent = _searchUIEvent.asStateFlow()
    private val searchResultsCache = mutableMapOf<String, Flow<PagingData<MediaUIState>>>()

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

        if (newSearchTerm == _uiState.value.searchInput) {
            return
        }

        _uiState.update { it.copy(searchInput = newSearchTerm) }

        if (newSearchTerm.isBlank()) {
            _uiState.update { it.copy(displayMode = SearchDisplayMode.SUGGESTIONS) }
            return
        }

        _uiState.update {
            it.copy(
                displayMode = SearchDisplayMode.SUGGESTIONS,
                isLoading = true
            )
        }
        viewModelScope.launch {
            performSearch(newSearchTerm)
        }
    }

    private suspend fun performSearch(searchTerm: String) {
        val currentState = _uiState.value

        when (currentState.searchTypes) {
            MediaTypes.MOVIE -> performMovieSearch(searchTerm)
            MediaTypes.TVS_SHOW -> performSeriesSearch(searchTerm)
            MediaTypes.ACTOR -> performActorSearch(searchTerm)
        }
    }

    private suspend fun performMovieSearch(searchTerm: String) {
        val cacheKey = "${MediaTypes.MOVIE}_$searchTerm"

        val cachedResult = searchResultsCache[cacheKey]
        if (cachedResult != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    searchResult = cachedResult,
                    hasValidResults = true,
                    lastSearchTerm = searchTerm,
                    lastSearchType = MediaTypes.MOVIE
                )
            }
            return
        }

        val searchResult = getSearchForMovieUseCase(searchTerm).map { pagingData ->
            pagingData.map { item -> searchMediaUIStateMapper.map(item) }
        }

        searchResultsCache[cacheKey] = searchResult

        _uiState.update {
            it.copy(
                isLoading = false,
                searchResult = searchResult,
                hasValidResults = true,
                lastSearchTerm = searchTerm,
                lastSearchType = MediaTypes.MOVIE
            )
        }
    }

    private suspend fun performSeriesSearch(searchTerm: String) {
        val cacheKey = "${MediaTypes.TVS_SHOW}_$searchTerm"

        val cachedResult = searchResultsCache[cacheKey]
        if (cachedResult != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    searchResult = cachedResult,
                    hasValidResults = true,
                    lastSearchTerm = searchTerm,
                    lastSearchType = MediaTypes.TVS_SHOW
                )
            }
            return
        }

        val searchResult = getSearchForSeriesUserCase(searchTerm).map { pagingData ->
            pagingData.map { item -> searchMediaUIStateMapper.map(item) }
        }

        searchResultsCache[cacheKey] = searchResult

        _uiState.update {
            it.copy(
                isLoading = false,
                searchResult = searchResult,
                hasValidResults = true,
                lastSearchTerm = searchTerm,
                lastSearchType = MediaTypes.TVS_SHOW
            )
        }
    }

    private suspend fun performActorSearch(searchTerm: String) {
        val cacheKey = "${MediaTypes.ACTOR}_$searchTerm"

        val cachedResult = searchResultsCache[cacheKey]
        if (cachedResult != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    searchResult = cachedResult,
                    hasValidResults = true,
                    lastSearchTerm = searchTerm,
                    lastSearchType = MediaTypes.ACTOR
                )
            }
            return
        }

        val searchResult = getSearchForActorUseCase(searchTerm).map { pagingData ->
            pagingData.map { item -> searchMediaUIStateMapper.map(item) }
        }

        searchResultsCache[cacheKey] = searchResult

        _uiState.update {
            it.copy(
                isLoading = false,
                searchResult = searchResult,
                hasValidResults = true,
                lastSearchTerm = searchTerm,
                lastSearchType = MediaTypes.ACTOR
            )
        }
    }

    fun setViewMode(viewMode: ViewMode) {
        val currentState = _uiState.value
        if (currentState.viewMode != viewMode) {
            _uiState.update {
                it.copy(
                    viewMode = viewMode,
                    lastViewMode = viewMode
                )
            }
        }
    }

    fun onSearchForMovie() {
        val currentState = _uiState.value
        _uiState.update { it.copy(searchTypes = MediaTypes.MOVIE) }
        if (currentState.searchInput.isNotBlank() &&
            currentState.displayMode == SearchDisplayMode.RESULTS) {

            val cacheKey = "${MediaTypes.MOVIE}_${currentState.searchInput}"
            val hasCache = searchResultsCache.containsKey(cacheKey)

            if (!hasCache) {
                _uiState.update { it.copy(isLoading = true) }
            }

            viewModelScope.launch {
                performMovieSearch(currentState.searchInput)
            }
        }
    }

    fun onSearchForSeries() {
        val currentState = _uiState.value
        _uiState.update { it.copy(searchTypes = MediaTypes.TVS_SHOW) }

        if (currentState.searchInput.isNotBlank() &&
            currentState.displayMode == SearchDisplayMode.RESULTS) {

            val cacheKey = "${MediaTypes.TVS_SHOW}_${currentState.searchInput}"
            val hasCache = searchResultsCache.containsKey(cacheKey)

            if (!hasCache) {
                _uiState.update { it.copy(isLoading = true) }
            }

            viewModelScope.launch {
                performSeriesSearch(currentState.searchInput)
            }
        }
    }

    fun onSearchForActor() {
        val currentState = _uiState.value
        _uiState.update { it.copy(searchTypes = MediaTypes.ACTOR) }
        if (currentState.searchInput.isNotBlank() &&
            currentState.displayMode == SearchDisplayMode.RESULTS) {
            val cacheKey = "${MediaTypes.ACTOR}_${currentState.searchInput}"
            val hasCache = searchResultsCache.containsKey(cacheKey)

            if (!hasCache) {
                _uiState.update { it.copy(isLoading = true) }
            }

            viewModelScope.launch {
                performActorSearch(currentState.searchInput)
            }
        }
    }

    fun onClearAllSearchHistory() {
        viewModelScope.launch {
            clearAllSearchHistoryUseCase()
        }
    }

    fun onDeleteSearchHistory(id: Long, name: String) {
        viewModelScope.launch {
            deleteSearchHistoryUseCase(id, name)
        }
    }

    override fun onClickActorResult(personID: Int, name: String) {
        saveSearchResult(personID, name)
        _searchUIEvent.update { Event(SearchUIEvent.ClickActorEvent(personID)) }
    }



    private fun saveSearchResult(id: Int, name: String) {
        viewModelScope.launch { postSaveSearchResultUseCase(id, name) }
    }

    override fun onClickSearchHistory(name: String) {
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
        val refreshState = combinedLoadStates.refresh

        if (refreshState is LoadState.Error) {
            if (itemCount == 0) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = listOf(Error(404, "An error occurred")),
                        isEmpty = false
                    )
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = refreshState is LoadState.Loading,
                    error = emptyList(),
                    isEmpty = refreshState is LoadState.NotLoading && itemCount < 1
                )
            }
        }
    }

    override fun onSuggestionClick(query: String) {
        if (query == _uiState.value.searchInput && _uiState.value.displayMode == SearchDisplayMode.RESULTS) {
            return
        }

        _uiState.update {
            it.copy(
                searchInput = query,
                displayMode = SearchDisplayMode.RESULTS,
                isLoading = true
            )
        }
        viewModelScope.launch {
            performSearch(query)
        }
    }


    override fun onMediaClick(media: MediaUIState) {
        saveSearchResult(media.mediaID, media.mediaName)
        _searchUIEvent.update { Event(SearchUIEvent.ClickMediaEvent(media)) }
    }
}