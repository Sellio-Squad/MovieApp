package com.karrar.movieapp.ui.actorDetails

import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.ActorDetails
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ActorDetailsUIMapper @Inject constructor() : Mapper<ActorDetails, ActorDetailsUIState> {
    override fun map(input: ActorDetails): ActorDetailsUIState {
        return ActorDetailsUIState(
            name = input.actorName,
            imageUrl = input.actorImage,
            gender = input.actorGender,
            birthday = toFormattedDate(input.actorBirthday),
            biography = input.actorBiography,
            placeOfBirth = input.actorPlaceOfBirth,
            knownFor = input.knownForDepartment,
        )
    }

    private fun toFormattedDate(inputDate: String): String{
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputFormat.parse(inputDate)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        return if(date != null) outputFormat.format(date) else "Jan 30, 1974"
    }
}