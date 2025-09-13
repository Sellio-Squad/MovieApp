package com.karrar.movieapp.ui.galleryActor

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.domain.usecases.GetActorGalleryUseCase
import com.karrar.movieapp.ui.actorDetails.ActorGalleriesUIMapper
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryActorViewModel @Inject constructor(
    state: SavedStateHandle,
    private val getActorGalleryUseCase: GetActorGalleryUseCase,
    private val actorGalleriesUIMapper: ActorGalleriesUIMapper,
    ): BaseViewModel(), GalleryActorInteractionListener {

    val args = GalleryActorFragmentArgs.fromSavedStateHandle(state)

    private val _galleryActorUIState = MutableStateFlow(GalleryActorUIState())
    val galleryActorUIState = _galleryActorUIState.asStateFlow()

    private val _galleryActorUIEvent: MutableStateFlow<Event<GalleryActorUIEvent?>> =
        MutableStateFlow(Event(null))
    val galleryActorUIEvent = _galleryActorUIEvent.asStateFlow()

    init {
        getData()
    }

    override fun getData() {
        _galleryActorUIState.update { it.copy(isLoading = true, error = emptyList()) }
        viewModelScope.launch {
            try {
                val actorGallery = getActorGalleryUseCase.invoke(args.id).galleryUrl
                Log.d("123abc123","size: ${actorGallery.size}")
                actorGallery.forEachIndexed { index, url->
                    Log.d("123abc123","image $index: $url")
                }
                _galleryActorUIState.update {
                    it.copy(
                        name = args.actorName,
                        imagesUrl = actorGallery,
                        isLoading = false,
                        isSuccess = true,
                    )

                }
                Log.d("123abc123","gallery : ${_galleryActorUIState.value}")

            }catch (e: Exception){
                Log.d("123abc123","error : ${e.message}")

                onError(e.message.toString())
            }
        }
    }

    private fun onError(message: String) {
        _galleryActorUIState.update { galleryActorUIState ->
            galleryActorUIState.copy(
                isLoading = false,
                error = listOf(Error(message)),
            )
        }
    }

    fun onClickBack() {
        _galleryActorUIEvent.update { Event(GalleryActorUIEvent.BackEvent) }
    }
}