package com.karrar.movieapp.domain.mappers.movie

import com.karrar.movieapp.BuildConfig
import com.karrar.movieapp.data.remote.response.MovieDto
import com.karrar.movieapp.domain.enums.MediaType
import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.Media
import com.karrar.movieapp.domain.models.SearchMedia
import com.karrar.movieapp.utilities.Constants
import javax.inject.Inject

class SearchMovieMapper @Inject constructor() : Mapper<MovieDto, SearchMedia> {
    override fun map(input: MovieDto): SearchMedia {
        val genresNames = input.genreIds?.map { genreIdToName(it ?: 0) } ?: emptyList()
        return SearchMedia(
            input.id ?: 0,
            BuildConfig.IMAGE_BASE_PATH + input.posterPath,
            MediaType.MOVIE.value,
            input.originalTitle ?: "",
            input.releaseDate ?: "",
            input.voteAverage?.toFloat() ?: 0f,
            genresNames
        )
    }

    private fun genreIdToName(id: Int): String {
        val genreMap = mapOf(
            28 to "Action",
            12 to "Adventure",
            16 to "Animation",
            35 to "Comedy",
            80 to "Crime",
            99 to "Documentary",
            18 to "Drama",
            10751 to "Family",
            14 to "Fantasy",
            36 to "History",
            27 to "Horror",
            10402 to "Music",
            9648 to "Mystery",
            10749 to "Romance",
            878 to "Science Fiction",
            10770 to "TV Movie",
            53 to "Thriller",
            10752 to "War",
            37 to "Western"
        )
        return genreMap[id] ?: "Unknown"
    }
}