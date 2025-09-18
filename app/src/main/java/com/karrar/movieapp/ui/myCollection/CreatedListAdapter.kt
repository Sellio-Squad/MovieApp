package com.karrar.movieapp.ui.myCollection

import com.karrar.movieapp.R
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener
import com.karrar.movieapp.ui.myCollection.myCollectionUIState.CreatedListUIState

class CreatedListAdapter(items: List<CreatedListUIState>, listener: CreatedListInteractionListener) :
    BaseAdapter<CreatedListUIState>(items, listener) {
    override val layoutID: Int = R.layout.item_saved_list
}

interface CreatedListInteractionListener : BaseInteractionListener {
    fun onListClick(item: CreatedListUIState)
}