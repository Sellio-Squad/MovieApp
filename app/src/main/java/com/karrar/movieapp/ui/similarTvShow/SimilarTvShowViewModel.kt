package com.karrar.movieapp.ui.similarTvShow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.domain.usecases.tvShowDetails.GetTvShowDetailsUseCase
import com.karrar.movieapp.ui.base.BaseInteractionListener
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.ui.explore.exploreUIState.ViewMode
import com.karrar.movieapp.ui.tvShowDetails.tvShowUIMapper.SimilarTvShowUIStateMapper
import com.karrar.movieapp.ui.tvShowDetails.tvShowUIState.Error
import com.karrar.movieapp.ui.tvShowDetails.tvShowUIState.TvShowDetailsUIState
import com.karrar.movieapp.utilities.Constants
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimilarTvShowViewModel @Inject constructor(
    private val getSimilarTvShows: GetTvShowDetailsUseCase,
    private val similarTvShowUIStateMapper: SimilarTvShowUIStateMapper,
    state: SavedStateHandle
) : BaseViewModel(), BaseInteractionListener {

    private val args = SimilarTvShowFragmentArgs.fromSavedStateHandle(state)

    private val _uiState = MutableStateFlow(TvShowDetailsUIState())
    val uiState: StateFlow<TvShowDetailsUIState> = _uiState.asStateFlow()

    private val _SimilarTvShowUIEvent: MutableStateFlow<Event<SimilarTvShowUIEvent>?> =
        MutableStateFlow(null)
    val SimilarTvShowUIEvent = _SimilarTvShowUIEvent.asStateFlow()

    init {
        getData()
    }

    override fun getData() {
        _uiState.update { it.copy(isLoading = true, errorUIState = emptyList()) }
        viewModelScope.launch {
            try {
                val result = getSimilarTvShows.getSimilarTvShow(args.mediaId)
                _uiState.update {
                    it.copy(
                        similarTvShowResult = result.map { media ->
                            similarTvShowUIStateMapper.map(media)
                        },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorUIState = onAddMessageToListError(e), isLoading = false)
                }
            }
        }
    }

    private fun onAddMessageToListError(e: Exception): List<Error> {
        return listOf(
            Error(
                code = Constants.INTERNET_STATUS,
                message = e.message.toString()
            )
        )
    }

    fun setViewMode(viewMode: ViewMode) {
        _uiState.update { it.copy(viewMode = viewMode) }
    }

    fun onClickTvShow(tvShowID: Int) {
//        _SimilarTvShowUIEvent.value = Event(SimilarTvShowUIEvent)

    }
}
