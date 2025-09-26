package com.karrar.movieapp.ui.tvShowDetails.ratingTvShowDialog

sealed interface RatingTvShowDialogEvents {
    object CloseDialogEvent : RatingTvShowDialogEvents
    object MessageAppear : RatingTvShowDialogEvents
}