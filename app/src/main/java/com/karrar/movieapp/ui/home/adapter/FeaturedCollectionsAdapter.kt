package com.karrar.movieapp.ui.home.adapter

import com.karrar.movieapp.R
import com.karrar.movieapp.ui.adapters.MovieInteractionListener
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.home.model.FeaturedCollectionsItem

class FeaturedCollectionsAdapter(
    items: List<FeaturedCollectionsItem>,
    val listener: MovieInteractionListener
) :
    BaseAdapter<FeaturedCollectionsItem>(items, listener) {
    override val layoutID: Int = R.layout.item_featured_collections
}
