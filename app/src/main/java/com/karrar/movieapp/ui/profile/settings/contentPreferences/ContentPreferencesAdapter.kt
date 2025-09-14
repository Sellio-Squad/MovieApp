package com.karrar.movieapp.ui.profile.settings.contentPreferences

import com.karrar.movieapp.R
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener

class ContentPreferencesAdapter(
    items: List<ContentPreferencesTypes>,
    listener: ContentPreferencesInteractionListener
) : BaseAdapter<ContentPreferencesTypes>(items, listener) {
    override val layoutID: Int = R.layout.item_preference_option
}

interface ContentPreferencesInteractionListener : BaseInteractionListener {
    fun onPreferenceSelected(item: ContentPreferencesTypes)
}