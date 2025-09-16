package com.karrar.movieapp.ui.actorDetails.actorSocial

import com.karrar.movieapp.R
import com.karrar.movieapp.ui.base.BaseAdapter

class SocialAdapter(val items: List<SocialItemUIState>, val listener: SocialInteractionListener) :
    BaseAdapter<SocialItemUIState>(items, listener) {

    override val layoutID: Int = R.layout.item_social

}