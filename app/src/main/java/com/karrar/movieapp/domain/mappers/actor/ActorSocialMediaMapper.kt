package com.karrar.movieapp.domain.mappers.actor

import com.karrar.movieapp.data.remote.response.actor.ActorSocialMediaDto
import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.ActorSocial
import javax.inject.Inject

class ActorSocialMediaMapper @Inject constructor() : Mapper<ActorSocialMediaDto, ActorSocial> {
    override fun map(input: ActorSocialMediaDto): ActorSocial {
        return ActorSocial(
            youtubeLink = if (!input.youtubeId.isNullOrBlank()) "https://www.youtube.com/@${input.youtubeId}" else "",
            facebookLink = if(!input.facebookId.isNullOrBlank()) "https://www.facebook.com/$${input.facebookId}" else "",
            instagramLink = if(!input.instagramId.isNullOrBlank()) "https://www.instagram.com/$${input.instagramId}" else "",
            twitterLink = if(!input.twitterId.isNullOrBlank()) "https://www.twitter.com/$${input.twitterId}" else "",
            tiktokLink = if(!input.tiktokId.isNullOrBlank()) "https://www.tiktok.com/@${input.tiktokId}" else ""
        )
    }
}