package com.karrar.movieapp.domain.mappers.movie

import com.karrar.movieapp.BuildConfig
import com.karrar.movieapp.data.local.database.entity.movie.MatchVibesMovieEntity
import com.karrar.movieapp.domain.enums.MediaType
import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.Media
import javax.inject.Inject

class MatchVibesMovieToMediaMapper @Inject constructor() : Mapper<MatchVibesMovieEntity, Media> {
    override fun map(input: MatchVibesMovieEntity): Media {
        return Media(
            mediaID = input.id,
            mediaImage = BuildConfig.IMAGE_BASE_PATH + input.imageUrl,
            mediaType = MediaType.MOVIE.value,
            mediaName = input.title,
            mediaDate = "",
            mediaRate = input.movieRate.toFloat()
        )
    }
}