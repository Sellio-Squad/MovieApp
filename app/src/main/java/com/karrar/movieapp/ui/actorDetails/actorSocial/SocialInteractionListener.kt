package com.karrar.movieapp.ui.actorDetails.actorSocial

import com.karrar.movieapp.ui.base.BaseInteractionListener

interface SocialInteractionListener: BaseInteractionListener{
    fun onSocialClick(url: String)
}