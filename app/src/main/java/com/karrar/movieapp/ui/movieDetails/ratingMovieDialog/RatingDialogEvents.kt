package com.karrar.movieapp.ui.movieDetails.ratingMovieDialog

sealed interface RatingDialogEvents {
    object CloseDialogEvent : RatingDialogEvents
    object MessageAppear : RatingDialogEvents
}