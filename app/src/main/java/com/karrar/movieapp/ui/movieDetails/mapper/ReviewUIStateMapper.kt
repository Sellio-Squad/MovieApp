package com.karrar.movieapp.ui.movieDetails.mapper

import com.karrar.movieapp.domain.mappers.Mapper
import com.karrar.movieapp.domain.models.Review
import com.karrar.movieapp.ui.movieDetails.movieDetailsUIState.ReviewUIState
import com.karrar.movieapp.utilities.DateFormatter
import javax.inject.Inject

class ReviewUIStateMapper @Inject constructor() : Mapper<Review, ReviewUIState> {
    override fun map(input: Review): ReviewUIState {
        return ReviewUIState(
            content = input.content,
            createDate = DateFormatter.formatApiDate(input.createDate),
            userImage = input.userImage,
            name = input.name,
            userName = input.userName,
            rating = input.rating
        )
    }
}