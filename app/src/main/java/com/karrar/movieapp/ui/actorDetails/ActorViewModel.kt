package com.karrar.movieapp.ui.actorDetails

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.R
import com.karrar.movieapp.domain.enums.HomeItemsType
import com.karrar.movieapp.domain.usecases.GetActorDetailsUseCase
import com.karrar.movieapp.domain.usecases.GetActorGalleryUseCase
import com.karrar.movieapp.domain.usecases.GetActorMoviesUseCase
import com.karrar.movieapp.domain.usecases.GetActorSocialMediaUseCase
import com.karrar.movieapp.ui.actorDetails.actorSocial.ActorSocialMediaUIMapper
import com.karrar.movieapp.ui.actorDetails.actorSocial.ActorSocialUIState
import com.karrar.movieapp.ui.actorDetails.actorSocial.SocialInteractionListener
import com.karrar.movieapp.ui.actorDetails.actorSocial.SocialItemUIState
import com.karrar.movieapp.ui.adapters.MovieInteractionListener
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActorViewModel @Inject constructor(
    state: SavedStateHandle,
    private val getActorDetailsUseCase: GetActorDetailsUseCase,
    private val getActorMoviesUseCase: GetActorMoviesUseCase,
    private val getActorGalleryUseCase: GetActorGalleryUseCase,
    private val getActorSocialMediaUseCase: GetActorSocialMediaUseCase,
    private val actorDetailsUIMapper: ActorDetailsUIMapper,
    private val actorMoviesUIMapper: ActorMoviesUIMapper,
    private val actorGalleriesUIMapper: ActorGalleriesUIMapper,
    private val actorSocialMediaUIMapper: ActorSocialMediaUIMapper,
) : BaseViewModel(), MovieInteractionListener, SocialInteractionListener {

    val args = ActorDetailsFragmentArgs.fromSavedStateHandle(state)

    private val _actorDetailsUIState = MutableStateFlow(ActorDetailsUIState())
    val actorDetailsUIState = _actorDetailsUIState.asStateFlow()

    private val _actorDetailsUIEvent: MutableStateFlow<Event<ActorDetailsUIEvent?>> =
        MutableStateFlow(Event(null))
    val actorDetailsUIEvent = _actorDetailsUIEvent.asStateFlow()

    init {
        getData()
    }

    override fun getData() {
        _actorDetailsUIState.update { it.copy(isLoading = true, error = emptyList()) }
        viewModelScope.launch {
            try {
                val actorDetails = actorDetailsUIMapper.map(getActorDetailsUseCase(args.id))
                val actorGallery = actorGalleriesUIMapper.map(getActorGalleryUseCase(args.id))
                val actorSocial = actorSocialMediaUIMapper.map(getActorSocialMediaUseCase(args.id))
                val galleryImage = actorGallery.galleryUrl
                val actorMovies = getActorMoviesUseCase(args.id).map { actorMoviesUIMapper.map(it) }
                val socialItems = actorSocial.toList()
                _actorDetailsUIState.update {
                    it.copy(
                        name = actorDetails.name,
                        gender = actorDetails.gender,
                        imageUrl = actorDetails.imageUrl,
                        placeOfBirth = actorDetails.placeOfBirth,
                        biography = actorDetails.biography,
                        birthday = actorDetails.birthday,
                        knownFor = actorDetails.knownFor,
                        actorGalleryUIState = actorGallery.copy(
                            firstImageUrl = galleryImage.getOrNull(0),
                            secondImageUrl = galleryImage.getOrNull(1),
                            thirdImageUrl = galleryImage.getOrNull(3)
                        ),
                        actorSocialUIState = actorSocial,
                        socialItemUIState = socialItems,
                        actorMovies = actorMovies,
                        isLoading = false,
                        isSuccess = true
                    )
                }
                Log.d("123abc123", "state: ${_actorDetailsUIState.value.socialItemUIState} :: actorSocial $actorSocial")

            } catch (e: Exception) {
                onError(e.message.toString())
            }
        }

    }

    private fun onError(message: String) {
        _actorDetailsUIState.update { actorDetailsUIState ->
            actorDetailsUIState.copy(
                isLoading = false,
                error = listOf(Error(message)),
            )
        }
    }

    fun onClickBack() {
        _actorDetailsUIEvent.update { Event(ActorDetailsUIEvent.BackEvent) }
    }

    override fun onClickMovie(movieId: Int) {
        _actorDetailsUIEvent.update { Event(ActorDetailsUIEvent.ClickMovieEvent(movieId)) }
    }

    override fun onClickSeeAllMovie(homeItemsType: HomeItemsType) {
        _actorDetailsUIEvent.update { Event(ActorDetailsUIEvent.SeeAllMovies) }
    }

    override fun onClickSeeAllGallery(homeItemsType: HomeItemsType) {
        _actorDetailsUIEvent.update { Event(ActorDetailsUIEvent.SeeAllGallery) }
    }

    override fun onSocialClick(url: String) {
        _actorDetailsUIEvent.update { Event(ActorDetailsUIEvent.ClickSocialItem(url)) }
    }

    private fun ActorSocialUIState.toList(): List<SocialItemUIState>{
        val socials = mutableListOf<SocialItemUIState>()
        if(facebookLink.isNotBlank()) socials.add(SocialItemUIState(R.drawable.colored_facebook,R.string.facebook, facebookLink))
        if(twitterLink.isNotBlank()) socials.add(SocialItemUIState(R.drawable.colored_x,R.string.twitter, twitterLink))
        if(youtubeLink.isNotBlank()) socials.add(SocialItemUIState(R.drawable.colored_youtube, R.string.youtube, youtubeLink))
        if(tiktokLink.isNotBlank()) socials.add(SocialItemUIState(R.drawable.colored_tiktok, R.string.tiktok, tiktokLink))
        if(instagramLink.isNotBlank()) socials.add(SocialItemUIState(R.drawable.colored_instagram, R.string.instagram, instagramLink))
        return socials
    }

}