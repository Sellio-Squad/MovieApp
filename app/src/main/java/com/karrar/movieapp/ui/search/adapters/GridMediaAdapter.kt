package com.karrar.movieapp.ui.search.adapters

import androidx.recyclerview.widget.DiffUtil
import com.karrar.movieapp.R
import com.karrar.movieapp.ui.base.*
import com.karrar.movieapp.ui.search.mediaSearchUIState.MediaUIState

class GridMediaAdapter(listener: MediaSearchInteractionListener)
    : BasePagingAdapter<MediaUIState>(GridMediaComparator, listener){
    override val layoutID: Int = R.layout.grid_media_item

    object GridMediaComparator : DiffUtil.ItemCallback<MediaUIState>(){
        override fun areItemsTheSame(oldItem: MediaUIState, newItem: MediaUIState) =
            oldItem.mediaID == newItem.mediaID

        override fun areContentsTheSame(oldItem: MediaUIState, newItem: MediaUIState) =
            oldItem == newItem
    }
}
