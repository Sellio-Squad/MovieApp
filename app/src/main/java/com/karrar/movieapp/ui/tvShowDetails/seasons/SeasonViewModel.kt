package com.karrar.movieapp.ui.tvShowDetails.seasons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.domain.usecases.tvShowDetails.GetTvShowDetailsUseCase
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.ui.tvShowDetails.SeasonInteractionListener
import com.karrar.movieapp.ui.tvShowDetails.tvShowUIMapper.TvShowSeasonUIMapper
import com.karrar.movieapp.ui.tvShowDetails.tvShowUIState.Error
import com.karrar.movieapp.ui.tvShowDetails.tvShowUIState.TvShowDetailsUIState
import com.karrar.movieapp.utilities.Constants
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeasonViewModel @Inject constructor(
    private val getSeasonsUseCase: GetTvShowDetailsUseCase,
    private val tvhShowsSeasonMapper: TvShowSeasonUIMapper,
    state: SavedStateHandle
) : BaseViewModel(), SeasonInteractionListener {
    private val args = SeasonFragmentArgs.fromSavedStateHandle(state)
    private val _uiState = MutableStateFlow(TvShowDetailsUIState())
    val uiState = _uiState.asStateFlow()

    private val _seasonsUIEvent = MutableStateFlow<Event<SeasonsUIEvent?>>(Event(null))
    val seasonsUIEvent = _seasonsUIEvent.asStateFlow()

    init {
        getData()
    }

    override fun getData() {
        _uiState.update { it.copy(isLoading = false, errorUIState = emptyList()) }
        viewModelScope.launch {
            try {
                val result = getSeasonsUseCase.getSeasons(args.tvShowId)
                _uiState.update {
                    it.copy(
                        seriesSeasonsResult = result.map { season -> tvhShowsSeasonMapper.map(season) },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorUIState = listOf(
                            Error(
                                code = Constants.INTERNET_STATUS,
                                message = e.message.toString()
                            )
                        ), isLoading = false
                    )
                }
            }
        }
    }

    override fun onBackClick() {
        _seasonsUIEvent.update { Event(SeasonsUIEvent.OnBackClick) }
    }
}