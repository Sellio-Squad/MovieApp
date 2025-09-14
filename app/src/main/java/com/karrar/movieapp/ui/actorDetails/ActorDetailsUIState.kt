package com.karrar.movieapp.ui.actorDetails

import com.karrar.movieapp.ui.actorDetails.actorSocial.ActorSocialUIState
import com.karrar.movieapp.ui.actorDetails.actorSocial.SocialItemUIState

data class ActorDetailsUIState(
    val name: String = "",
    val imageUrl: String = "",
    val gender: String = "",
    val birthday: String = "",
    val placeOfBirth: String = "",
    val knownFor: String = "",
    val biography: String = "",
    val actorGalleryUIState: ActorGalleryUIState = ActorGalleryUIState(0,emptyList()),
    val actorSocialUIState: ActorSocialUIState = ActorSocialUIState(),
    val socialItemUIState: List<SocialItemUIState> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: List<Error> = emptyList(),
    val actorMovies: List<ActorMoviesUIState> = emptyList()
)