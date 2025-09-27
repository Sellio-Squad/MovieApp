package com.karrar.movieapp.domain.mappers.series

import com.karrar.movieapp.BuildConfig
import com.karrar.movieapp.data.remote.response.SeasonDto
import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.Season
import javax.inject.Inject

class SeasonMapper @Inject constructor(
    private val episodeMapper: EpisodeMapper
) : Mapper<SeasonDto, Season> {
    override fun map(input: SeasonDto): Season {
        return Season(
            input.id ?: 0,
            BuildConfig.IMAGE_BASE_PATH + input.posterPath,
            input.name ?: "",
            extractYearFromDate(input.airDate),
            input.seasonNumber ?: 0,
            seasonRate = input.seasonRate ?: 0.0f,
            input.episodeCount ?: 0,
            input.overview ?: "",
            input.episodes?.map {
                episodeMapper.map(it)
            } ?: emptyList()
        )
    }

    private fun extractYearFromDate(dateString: String?): String {
        return if (dateString.isNullOrEmpty()) {
            ""
        } else {
            try {
               dateString.substring(0, 4)
            } catch (e: Exception) {
                ""
            }
        }
    }
}
