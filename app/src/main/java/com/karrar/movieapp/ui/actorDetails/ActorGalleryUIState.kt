package com.karrar.movieapp.ui.actorDetails

data class ActorGalleryUIState(
    val id: Int = 0,
    val galleryUrl: List<String> = emptyList(),
    val firstImageUrl: String? = "",
    val secondImageUrl: String? = "",
    val thirdImageUrl: String? = "",
)