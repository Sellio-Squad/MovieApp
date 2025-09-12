package com.karrar.movieapp.ui.galleryActor

data class GalleryUrlUIState(
    val images: List<String>,
    val isFlipped: Boolean = false
)