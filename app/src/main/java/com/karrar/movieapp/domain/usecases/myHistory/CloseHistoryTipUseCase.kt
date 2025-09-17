package com.karrar.movieapp.domain.usecases.myHistory

import com.karrar.movieapp.data.repository.HistoryTipsRepository
import javax.inject.Inject

class CloseHistoryTipUseCase @Inject constructor(
    private val historyTipsRepository: HistoryTipsRepository
) {
    suspend operator fun invoke() = historyTipsRepository.closeHistoryTip()
}