package com.karrar.movieapp.ui.adapters

import com.karrar.movieapp.R
import com.karrar.movieapp.domain.enums.TvShowItemsType
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener
import com.karrar.movieapp.ui.models.MediaUiState

class TvShowDetailsAdapter(items: List<MediaUiState>, val listener: TvShowDetailsInteractionListener) :
    BaseAdapter<MediaUiState>(items, listener) {
    override val layoutID: Int = R.layout.item_similar_series
}

interface TvShowDetailsInteractionListener : BaseInteractionListener {
    fun onClickTvShow(item: MediaUiState)
//    fun onClickSeeAllTvShows(tvShowItemsType: TvShowItemsType)
}