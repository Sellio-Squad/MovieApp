package com.karrar.movieapp.data.remote.response.actor

import com.google.gson.annotations.SerializedName

data class ActorGalleryDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("profiles")
    val profileDto: List<ProfileDto>? = null
){
    data class ProfileDto(
        @SerializedName("aspect_ratio")
        val aspectRatio: Double? = null,
        @SerializedName("height")
        val height: Int? = null,
        @SerializedName("iso_639_1")
        val iso6391: String? = null,
        @SerializedName("file_path")
        val filePath: String? = null,
        @SerializedName("vote_average")
        val voteAverage: Double? = null,
        @SerializedName("vote_count")
        val voteCount: Double? = null,
        @SerializedName("width")
        val width: Int? = null
    )
}
