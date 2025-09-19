package com.karrar.movieapp.ui.explore.adapter

import androidx.recyclerview.widget.DiffUtil
import com.karrar.movieapp.R
import com.karrar.movieapp.ui.base.BasePagingAdapter
import com.karrar.movieapp.ui.explore.CategoryInteractionListener
import com.karrar.movieapp.ui.explore.exploreUIState.MediaUIState

class CategoryListAdapter(listener: CategoryInteractionListener) :
    BasePagingAdapter<MediaUIState>(MediaComparator, listener) {
    override val layoutID: Int = R.layout.item_media_card_explore

    object MediaComparator : DiffUtil.ItemCallback<MediaUIState>() {
        override fun areItemsTheSame(oldItem: MediaUIState, newItem: MediaUIState) =
            oldItem.mediaID == newItem.mediaID

        override fun areContentsTheSame(oldItem: MediaUIState, newItem: MediaUIState) =
            oldItem == newItem
    }

}