package com.karrar.movieapp.data.remote.response


import com.google.gson.annotations.SerializedName
import com.karrar.movieapp.data.remote.response.actor.ActorDto

data class CreditsDto(
    @SerializedName("crew")
    val crew: List<CrewDto>? = listOf(),
    @SerializedName("cast")
    val cast: List<ActorDto>? = listOf(),
    @SerializedName("id")
    val id: Int? = 0
)