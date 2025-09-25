package com.karrar.movieapp.ui.explore

import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.Media
import com.karrar.movieapp.domain.models.SearchMedia
import com.karrar.movieapp.ui.explore.exploreUIState.MediaUIState
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class MediaUIStateMapper @Inject constructor() : Mapper<SearchMedia, MediaUIState> {
    override fun map(input: SearchMedia): MediaUIState {
        return MediaUIState(
            mediaID = input.mediaID,
            mediaImage = input.mediaImage,
            mediaType = input.mediaType,
            mediaName = input.mediaName,
            mediaVoteAverage = input.mediaRate,
            mediaReleaseDate = toUiDate(input.mediaDate),
            input.mediaGenre.joinToString(", ")
        )
    }


    private fun toUiDate(inputDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputFormat = SimpleDateFormat("yyyy, MMM dd", Locale.US)
            val date = inputFormat.parse(inputDate)
            date?.let { outputFormat.format(it) } ?: inputDate
        } catch (e: Exception) {
            inputDate
        }
    }
}