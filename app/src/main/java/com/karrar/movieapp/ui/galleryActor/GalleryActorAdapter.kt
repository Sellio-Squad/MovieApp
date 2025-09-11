package com.karrar.movieapp.ui.galleryActor

import com.karrar.movieapp.R
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener

class GalleryActorAdapter(
    items: List<GalleryUrlUIState>,
    val listener: GalleryActorInteractionListener
) : BaseAdapter<GalleryUrlUIState>(items,listener) {
    override val layoutID: Int = R.layout.item_gallery

}

interface GalleryActorInteractionListener : BaseInteractionListener