package com.karrar.movieapp.ui.search.adapters

import androidx.recyclerview.widget.DiffUtil
import com.karrar.movieapp.R
import com.karrar.movieapp.ui.base.*
import com.karrar.movieapp.ui.search.SearchViewModel
import com.karrar.movieapp.ui.search.mediaSearchUIState.MediaUIState


class MediaSearchCardAdapter(listener: SearchViewModel)
    : BasePagingAdapter<MediaUIState>(MediaSearchCardComparator, listener){
    override val layoutID: Int = R.layout.list_media_item

    object MediaSearchCardComparator : DiffUtil.ItemCallback<MediaUIState>(){
        override fun areItemsTheSame(oldItem: MediaUIState, newItem: MediaUIState) =
            oldItem.mediaID == newItem.mediaID

        override fun areContentsTheSame(oldItem: MediaUIState, newItem: MediaUIState) =
            oldItem == newItem
    }
}

