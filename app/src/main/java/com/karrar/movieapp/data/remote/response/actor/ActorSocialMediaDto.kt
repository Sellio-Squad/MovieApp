package com.karrar.movieapp.data.remote.response.actor
import com.google.gson.annotations.SerializedName


data class ActorSocialMediaDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("wikidata_id")
    val wikidataId: String?,
    @SerializedName("facebook_id")
    val facebookId: String?,
    @SerializedName("tiktok_id")
    val tiktokId: String?,
    @SerializedName("twitter_id")
    val twitterId: String?,
    @SerializedName("youtube_id")
    val youtubeId: String?,
    @SerializedName("instagram_id")
    val instagramId: String?,
    @SerializedName("freebase_id")
    val freebaseId: String?,
    @SerializedName("freebase_mid")
    val freebaseMid: String?,
    @SerializedName("imdb_id")
    val imdbId: String?
)