package com.karrar.movieapp.ui.similarTvShow

import com.karrar.movieapp.R
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener
import com.karrar.movieapp.ui.models.MediaUiState

class SimilarTvShowsAdapter(
    items: List<MediaUiState>, listener: BaseInteractionListener
) : BaseAdapter<MediaUiState>(items, listener) {

    override val layoutID: Int = R.layout.item_similar_series
}