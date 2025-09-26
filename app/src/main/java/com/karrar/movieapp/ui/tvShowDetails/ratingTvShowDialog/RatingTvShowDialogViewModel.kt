package com.karrar.movieapp.ui.tvShowDetails.ratingTvShowDialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.domain.usecases.tvShowDetails.GetTvShowDetailsUseCase
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RatingTvShowDialogViewModel @Inject constructor(
    state: SavedStateHandle,
    private val setTvShowRatingUseCase: com.karrar.movieapp.domain.usecases.tvShowDetails.SetRatingUesCase,
    private val getTvShowDetailsUseCase: GetTvShowDetailsUseCase,
) : BaseViewModel(), RatingTvShowDialogInteractionListener {

    private val args = RatingTvShowDialogArgs.fromSavedStateHandle(state)

    private val _uiState = MutableStateFlow(RatingTvShowDialogUiState())
    val uiState: StateFlow<RatingTvShowDialogUiState> = _uiState.asStateFlow()

    private val _ratingDialogEvent: MutableStateFlow<Event<RatingTvShowDialogEvents?>> =
        MutableStateFlow(Event(null))
    val ratingDialogEvent = _ratingDialogEvent.asStateFlow()


    init {
        getData()
    }

    override fun getData() {
        getRatedTvShow(args.tvShowId)
    }

    private fun getRatedTvShow(tvShowId: Int) {
        viewModelScope.launch {
            try {
                val value = getTvShowDetailsUseCase.getTvShowRated(tvShowId)
                _uiState.update { it.copy(ratingValue = value, editMode = (value != 0f)) }
            } catch (e: Throwable) {
            }
        }
    }

    override fun onChangeRating(value: Float) {
        _uiState.update { it.copy(ratingValue = value) }
    }

    override fun onSubmitRating() {
        viewModelScope.launch {
            try {
                val value = uiState.value.ratingValue
                setTvShowRatingUseCase(args.tvShowId, value)
                onCancel()
                _ratingDialogEvent.update { Event(RatingTvShowDialogEvents.MessageAppear) }
            } catch (e: Throwable) {
            }
        }
    }

    override fun onCancel() {
        _ratingDialogEvent.update { Event(RatingTvShowDialogEvents.CloseDialogEvent) }
    }

    override fun onRemoveRating() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(ratingValue = 0f) }
                setTvShowRatingUseCase(args.tvShowId, 0f)
                onCancel()
                _ratingDialogEvent.update { Event(RatingTvShowDialogEvents.MessageAppear) }
            } catch (e: Throwable) {
            }
        }
    }
}