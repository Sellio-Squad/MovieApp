package com.karrar.movieapp.ui.home.adapter

import com.karrar.movieapp.R
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener
import com.karrar.movieapp.ui.home.homeUiState.FeaturedCollectionUiState

class FeaturedCollectionsAdapter(
    items: List<FeaturedCollectionUiState>,
    val listener: FeaturedCollectionsListener
) :
    BaseAdapter<FeaturedCollectionUiState>(items, listener) {
    override val layoutID: Int = R.layout.item_featured_collections
}

interface FeaturedCollectionsListener : BaseInteractionListener {
    fun onClickCollection(title: String)
}