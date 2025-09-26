package com.karrar.movieapp.ui.movieDetails.createList

import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.domain.usecases.mylist.CreateMovieListUseCase
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.ui.movieDetails.createList.CreatedListUIMapper
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CreateListViewModel @Inject constructor(
    private val createMovieListUseCase: CreateMovieListUseCase,
    private val createdListUIMapper: CreatedListUIMapper,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(CreateListDialogUIState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableStateFlow<Event<CreateListUIEvent?>>(Event(null))
    val events = _events.asStateFlow()

    init {
        getData()
    }


    override fun getData() {
    }

    fun onListNameChanged(name: CharSequence?) {
        _uiState.update { it.copy(mediaListName = name?.toString().orEmpty()) }
    }


    fun onCreateList() {
        viewModelScope.launch {
            try {
                createMovieListUseCase(_uiState.value.mediaListName)
                    .map { createdListUIMapper.map(it) }

                _events.value = Event(CreateListUIEvent.ListCreated)
            } catch (t: Throwable) {
                _events.value = Event(CreateListUIEvent.Error(t.message.orEmpty()))
            }
        }
    }


    fun onCancel() {
        _events.value = Event(CreateListUIEvent.Dismiss)
    }

}
