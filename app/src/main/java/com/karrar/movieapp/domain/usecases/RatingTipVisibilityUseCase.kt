package com.karrar.movieapp.domain.usecases

import com.karrar.movieapp.data.repository.RatingTipsRepository
import jakarta.inject.Inject

class RatingTipVisibilityUseCase @Inject constructor(
    private val ratingTipsRepository: RatingTipsRepository
) {
    suspend fun showTip() = ratingTipsRepository.showRatingTip()
    suspend fun hideTip() = ratingTipsRepository.hideRatingTip()
}