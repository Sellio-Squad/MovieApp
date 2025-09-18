package com.karrar.movieapp.ui.myCollection.collectionDetails

import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.SaveListDetails
import com.karrar.movieapp.ui.myCollection.collectionDetails.collectionDetailsUIState.SavedMediaUIState
import javax.inject.Inject

class MediaUIStateMapper @Inject constructor() : Mapper<SaveListDetails, SavedMediaUIState> {

    override fun map(input: SaveListDetails): SavedMediaUIState {
        return SavedMediaUIState(
            image = input.posterPath,
            mediaID = input.id,
            title = input.title,
            voteAverage = input.voteAverage,
            releaseDate = formatDate(input.releaseDate),
            mediaType = input.mediaType,
            duration = input.duration.toString(),
            genres = input.genres
        )
    }

    private fun formatDate(date: String?) = date
        ?.replace("-", " ")
        ?.split(" ")
        ?.run {
            "${this[0]}, ${
                listOf(
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec"
                )[this[1].toInt()]
            } ${this[2]}"
        } ?: "Unknown"
}