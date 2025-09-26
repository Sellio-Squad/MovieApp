package com.karrar.movieapp.ui.movieDetails.ratingMovieDialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.domain.usecases.movieDetails.GetMovieRateUseCase
import com.karrar.movieapp.domain.usecases.movieDetails.SetRatingUseCase
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
class RatingMovieMovieDialogViewModel @Inject constructor(
    state: SavedStateHandle,
    private val setRatingUseCase: SetRatingUseCase,
    private val getMovieRateUseCase: GetMovieRateUseCase,
) : BaseViewModel(), RatingMovieDialogInteractionListener {

    private val args = RatingMovieDialogArgs.fromSavedStateHandle(state)

    private val _uiState = MutableStateFlow(RatingMovieDialogUiState())
    val uiState: StateFlow<RatingMovieDialogUiState> = _uiState.asStateFlow()

    private val _ratingDialogEvent: MutableStateFlow<Event<RatingDialogEvents?>> =
        MutableStateFlow(Event(null))
    val ratingDialogEvent = _ratingDialogEvent.asStateFlow()

    init {
        getData()
    }

    override fun getData() {
        getRatedMovie(args.movieId)
    }

    private fun getRatedMovie(movieId: Int) {
        viewModelScope.launch {
            try {
                val value = getMovieRateUseCase(movieId)
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
                setRatingUseCase(args.movieId, value)
                onCancel()
                _ratingDialogEvent.update { Event(RatingDialogEvents.MessageAppear) }
            } catch (e: Throwable) {
            }
        }
    }

    override fun onCancel() {
        _ratingDialogEvent.update { Event(RatingDialogEvents.CloseDialogEvent) }
    }

    override fun onRemoveRating() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(ratingValue = 0f) }
                setRatingUseCase(args.movieId, 0f)
                onCancel()
                _ratingDialogEvent.update { Event(RatingDialogEvents.MessageAppear) }
            } catch (e: Throwable) {
            }
        }
    }

}