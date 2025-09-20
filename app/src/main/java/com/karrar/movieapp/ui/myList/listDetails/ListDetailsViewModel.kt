package com.karrar.movieapp.ui.myList.listDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.domain.models.SaveListDetails
import com.karrar.movieapp.domain.usecases.movieDetails.GetMovieDetailsUseCase
import com.karrar.movieapp.domain.usecases.mylist.GetMyMediaListDetailsUseCase
import com.karrar.movieapp.domain.usecases.mylist.RemoveMovieFromListUseCase
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.ui.explore.exploreUIState.ErrorUIState
import com.karrar.movieapp.ui.myList.listDetails.listDetailsUIState.ListDetailsUIEvent
import com.karrar.movieapp.ui.myList.listDetails.listDetailsUIState.ListDetailsUIState
import com.karrar.movieapp.ui.myList.listDetails.listDetailsUIState.SavedMediaUIState
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.internal.concurrent.formatDuration
import javax.inject.Inject


@HiltViewModel
class ListDetailsViewModel @Inject constructor(
    private val getMyMediaListDetailsUseCase: GetMyMediaListDetailsUseCase,
    private val mediaUIStateMapper: MediaUIStateMapper,
    private val removeMovieFromListUseCase: RemoveMovieFromListUseCase,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    saveStateHandle: SavedStateHandle
) : BaseViewModel(), ListDetailsInteractionListener {

    val args = ListDetailsFragmentArgs.fromSavedStateHandle(saveStateHandle)

    private val _listDetailsUIState = MutableStateFlow(ListDetailsUIState())
    val listDetailsUIState = _listDetailsUIState.asStateFlow()

    private val _listDetailsUIEvent = MutableStateFlow<Event<ListDetailsUIEvent?>>(Event(null))
    val listDetailsUIEvent = _listDetailsUIEvent.asStateFlow()

    init {
        getData()
    }

    override fun getData() {
        _listDetailsUIState.update {
            it.copy(isLoading = true, isEmpty = false, error = emptyList())
        }
        viewModelScope.launch {
            try {
                val result = updateMoviesDurationTime(getMyMediaListDetailsUseCase(args.id))
                _listDetailsUIState.update {
                    it.copy(
                        isLoading = false,
                        isEmpty = result.isEmpty(),
                        savedMedia = result
                    )
                }

            } catch (t: Throwable) {
                _listDetailsUIState.update {
                    it.copy(
                        isLoading = false, error = listOf(
                            ErrorUIState(0, t.message.toString())
                        )
                    )
                }
            }
        }
    }

    private suspend fun updateMoviesDurationTime(movies: List<SaveListDetails>): List<SavedMediaUIState> {
        return movies.map { movie ->
            val movieDetails = getMovieDetailsUseCase.getMovieDetails(movie.id)
            mediaUIStateMapper.map(movie).copy(
                duration = formatDuration(movieDetails.movieDuration.toLong())
            )
        }
    }

    override fun onItemClick(item: SavedMediaUIState) {
        _listDetailsUIEvent.update { Event(ListDetailsUIEvent.OnItemSelected(item)) }
    }

    override fun onDeleteItem(item: Int) {
        viewModelScope.launch {
            try {
                removeMovieFromListUseCase(args.id, item)
                val currentList = _listDetailsUIState.value.savedMedia.toMutableList()
                currentList.removeAll { it.mediaID == item }

                _listDetailsUIState.update {
                    it.copy(
                        savedMedia = currentList,
                        isEmpty = currentList.isEmpty(),
                        error = emptyList()
                    )
                }
            } catch (t: Throwable) {
                _listDetailsUIState.update {
                    it.copy(error = listOf(ErrorUIState(0, t.message.toString())))
                }
            }
        }
    }

    fun closeTip() {
        _listDetailsUIState.update { it.copy(isTipShown = false) }
    }

}