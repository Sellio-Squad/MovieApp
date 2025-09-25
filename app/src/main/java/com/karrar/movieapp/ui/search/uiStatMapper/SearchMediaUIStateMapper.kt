package com.karrar.movieapp.ui.search.uiStatMapper

import android.util.Log
import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.SearchMedia
import com.karrar.movieapp.ui.search.mediaSearchUIState.MediaUIState
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


class SearchMediaUIStateMapper @Inject constructor() : Mapper<SearchMedia, MediaUIState> {
    override fun map(input: SearchMedia): MediaUIState {
        Log.i("mohamed", "$input")
        return MediaUIState(
            input.mediaID,
            input.mediaName,
            input.mediaImage,
            input.mediaType,
            input.mediaRate,
            toUiDate(input.mediaDate),
            input.mediaGenre.joinToString(", ")
        )
        Log.i("mohamed", "$input")
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