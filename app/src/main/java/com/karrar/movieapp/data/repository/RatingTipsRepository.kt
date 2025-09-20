package com.karrar.movieapp.data.repository

interface RatingTipsRepository {
    suspend fun showRatingTip(): Boolean
    suspend fun hideRatingTip()
}