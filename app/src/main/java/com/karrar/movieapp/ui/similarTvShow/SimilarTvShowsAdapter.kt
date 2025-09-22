package com.karrar.movieapp.ui.similarTvShow

import com.karrar.movieapp.R
import com.karrar.movieapp.domain.models.TvShowDetails
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener

class SimilarTvShowsAdapter(
    items: List<TvShowDetails>, listener: BaseInteractionListener
) : BaseAdapter<TvShowDetails>(items, listener) {

    override val layoutID: Int = R.layout.item_similar_series
}