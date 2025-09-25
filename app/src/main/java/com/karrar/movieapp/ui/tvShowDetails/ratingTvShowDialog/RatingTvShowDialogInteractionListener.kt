package com.karrar.movieapp.ui.tvShowDetails.ratingTvShowDialog

interface RatingTvShowDialogInteractionListener {
    fun onChangeRating(value: Float)
    fun onSubmitRating()
    fun onCancel()
    fun onRemoveRating()
}