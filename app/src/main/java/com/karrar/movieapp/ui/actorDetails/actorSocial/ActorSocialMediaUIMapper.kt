package com.karrar.movieapp.ui.actorDetails.actorSocial

import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.ActorSocial
import javax.inject.Inject

class ActorSocialMediaUIMapper @Inject constructor() : Mapper<ActorSocial, ActorSocialUIState> {
    override fun map(input: ActorSocial): ActorSocialUIState {
        return ActorSocialUIState(
            input.youtubeLink,
            input.facebookLink,
            input.instagramLink,
            input.twitterLink,
            input.tiktokLink
        )
    }
}