package com.karrar.movieapp.ui.movieDetails.ratingMovieDialog

interface RatingMovieDialogInteractionListener {
    fun onChangeRating(value: Float)
    fun onSubmitRating()
    fun onCancel()
    fun onRemoveRating()
}