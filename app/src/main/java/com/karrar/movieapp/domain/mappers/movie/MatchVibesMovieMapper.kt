package com.karrar.movieapp.domain.mappers.movie

import com.karrar.movieapp.BuildConfig
import com.karrar.movieapp.data.local.database.entity.movie.MatchVibesMovieEntity
import com.karrar.movieapp.domain.enums.MediaType
import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.Media
import javax.inject.Inject

class MatchVibesMovieMovieMapper @Inject constructor() : Mapper<MatchVibesMovieEntity, Media> {
    override fun map(input: MatchVibesMovieEntity): Media {
        return Media(
            input.id,
            BuildConfig.IMAGE_BASE_PATH + input.imageUrl,
            MediaType.MOVIE.value,
            input.title,
            "",
            input.movieRate.toFloat()
        )
    }
}