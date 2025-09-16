package com.karrar.movieapp.ui.explore

import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.map
import com.karrar.movieapp.domain.usecases.GetGenreListUseCase
import com.karrar.movieapp.domain.usecases.GetMediaByGenreIDUseCase
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.ui.explore.exploreUIState.ErrorUIState
import com.karrar.movieapp.ui.explore.exploreUIState.ExploreUIState
import com.karrar.movieapp.ui.explore.exploreUIState.ExploringUIEvent
import com.karrar.movieapp.ui.explore.exploreUIState.ViewMode
import com.karrar.movieapp.utilities.Constants
import com.karrar.movieapp.utilities.Constants.MOVIE_CATEGORIES_ID
import com.karrar.movieapp.utilities.Constants.TV_CATEGORIES_ID
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ExploringViewModel @Inject constructor(
    private val getCategoryUseCase: GetMediaByGenreIDUseCase,
    private val mediaUIStateMapper: MediaUIStateMapper,
    private val genreUIStateMapper: GenreUIStateMapper,
    private val getGenresUseCase: GetGenreListUseCase
) : BaseViewModel(), CategoryInteractionListener {

    private val _uiState = MutableStateFlow(ExploreUIState())
    val uiState: StateFlow<ExploreUIState> = _uiState

    private val _exploringUIEvent: MutableStateFlow<Event<ExploringUIEvent>?> =
        MutableStateFlow(null)
    val exploringUIEvent = _exploringUIEvent.asStateFlow()

    private var currentMediaType: Int = MOVIE_CATEGORIES_ID


    init {
        getData()
    }

    override fun getData() {
        _uiState.update { it.copy(isLoading = true, error = emptyList()) }
        viewModelScope.launch {
            try {
                getGenres(currentMediaType)
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = listOf(ErrorUIState(404, e.message ?: "Unknown error"))
                    )
                }
            }
        }
    }

    private fun getGenres(mediaType: Int) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val genres = getGenresUseCase(mediaType).map { genreUIStateMapper.map(it) }
                _uiState.update { 
                    it.copy(
                        genres = genres,
                        isLoading = false,
                        error = emptyList()
                    ) 
                }
                if (genres.isNotEmpty()) {
                    getMediaList(currentMediaType, genres.first().genreID)
                }
            } catch (t: Throwable) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = listOf(ErrorUIState(-1, t.message ?: "Error"))
                    ) 
                }
            }
        }
    }

    fun getMediaList(mediaType: Int, categoryId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = getCategoryUseCase(mediaType, categoryId)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    selectedGenreID = categoryId,
                    media = result.map { pagingData -> pagingData.map { mediaUIStateMapper.map(it) } },
                    error = emptyList()
                )
            }
        }
    }

    fun setViewMode(viewMode: ViewMode) {
        _uiState.update { it.copy(viewMode = viewMode) }
    }

    fun onClickSearch() {
        _exploringUIEvent.update { Event(ExploringUIEvent.SearchEvent) }
    }

    fun onClickMovies() {
        currentMediaType = MOVIE_CATEGORIES_ID
        _uiState.update { it.copy(currentType = Constants.MOVIE) }
        getData()
    }

    fun onClickTVShow() {
        currentMediaType = TV_CATEGORIES_ID
        _uiState.update { it.copy(currentType = Constants.TV_SHOWS) }
        getData()
    }

    override fun onClickCategory(categoryId: Int) {
        getMediaList(currentMediaType, categoryId)
        _exploringUIEvent.update { Event(ExploringUIEvent.SelectedCategory(categoryId)) }
    }

    fun onTabChanged(position: Int) {
        when (position) {
            0 -> { onClickMovies() }
            1 -> { onClickTVShow() }
        }
    }


    fun onClickMedia(mediaId: Int) {
        _exploringUIEvent.update { Event(ExploringUIEvent.ClickMediaEvent(mediaId)) }
    }

    fun onClickActors() {
        _exploringUIEvent.update { Event(ExploringUIEvent.ActorsEvent) }
    }
    
    fun setErrorUiState(loadState: LoadState) {
        when (loadState) {
            is LoadState.Error -> {
                _uiState.update {
                    it.copy(
                        error = listOf(ErrorUIState(-1, loadState.error.message ?: "Unknown error")),
                        isLoading = false
                    )
                }
            }
            is LoadState.Loading -> {
                _uiState.update { it.copy(isLoading = true) }
            }
            is LoadState.NotLoading -> {
                _uiState.update { it.copy(isLoading = false, error = emptyList()) }
            }
        }
    }


}