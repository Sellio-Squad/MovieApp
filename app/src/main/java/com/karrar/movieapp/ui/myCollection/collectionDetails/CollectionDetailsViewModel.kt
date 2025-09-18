package com.karrar.movieapp.ui.myCollection.collectionDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.domain.models.SaveListDetails
import com.karrar.movieapp.domain.usecases.movieDetails.GetMovieDetailsUseCase
import com.karrar.movieapp.domain.usecases.myCollection.GetMyMediaListDetailsUseCase
import com.karrar.movieapp.domain.usecases.myCollection.RemoveMovieFromListUseCase
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.ui.category.uiState.ErrorUIState
import com.karrar.movieapp.ui.myCollection.collectionDetails.collectionDetailsUIState.CollectionDetailsUIEvent
import com.karrar.movieapp.ui.myCollection.collectionDetails.collectionDetailsUIState.CollectionDetailsUIState
import com.karrar.movieapp.ui.myCollection.collectionDetails.collectionDetailsUIState.SavedMediaUIState
import com.karrar.movieapp.utilities.Event
import com.karrar.movieapp.utilities.formatDuration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CollectionDetailsViewModel @Inject constructor(
    private val getMyMediaListDetailsUseCase: GetMyMediaListDetailsUseCase,
    private val mediaUIStateMapper: MediaUIStateMapper,
    private val removeMovieFromListUseCase: RemoveMovieFromListUseCase,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    saveStateHandle: SavedStateHandle
) : BaseViewModel(), ListDetailsInteractionListener {

    val args = CollectionDetailsFragmentArgs.fromSavedStateHandle(saveStateHandle)

    private val _listDetailsUIState = MutableStateFlow(CollectionDetailsUIState())
    val listDetailsUIState = _listDetailsUIState.asStateFlow()

    private val _listDetailsUIEvent = MutableStateFlow<Event<CollectionDetailsUIEvent?>>(Event(null))
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
                duration = formatDuration(movieDetails.movieDuration)
            )
        }
    }

    override fun onItemClick(item: SavedMediaUIState) {
        _listDetailsUIEvent.update { Event(CollectionDetailsUIEvent.OnItemSelected(item)) }
    }

    override fun onDeleteItem(itemId: Int) {
        viewModelScope.launch {
            try {
                removeMovieFromListUseCase(args.id, itemId)
                val currentList = _listDetailsUIState.value.savedMedia.toMutableList()
                currentList.removeAll { it.mediaID == itemId }

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


}

