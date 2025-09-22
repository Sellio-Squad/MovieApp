package com.karrar.movieapp.ui.tvShowDetails.tvShowUIState

import androidx.lifecycle.ViewModel
import com.karrar.movieapp.domain.enums.TvShowItemsType
import com.karrar.movieapp.ui.models.ActorUiState
import com.karrar.movieapp.ui.models.CrewUIState
import com.karrar.movieapp.ui.models.MediaUiState

sealed class DetailItemUIState(val priority: Int) {

    class OverView(val data: TvShowDetailsResultUIState) : DetailItemUIState(0)

    class Seasons(val data: List<SeasonUIState>) : DetailItemUIState(1)

    class Cast(val data: List<ActorUiState>) : DetailItemUIState(2)

    class Crew(val data: List<CrewUIState>) : DetailItemUIState(3)

    class SimilarTvShow(val data: List<MediaUiState>) : DetailItemUIState(4)

    class SeeAllSimilarTvShowButton(
        val data: List<MediaUiState>,
    ) : DetailItemUIState(5)

    class Rating(val viewModel: ViewModel) : DetailItemUIState(6)

    object SeeAllReviewsButton : DetailItemUIState(7)

    class Comment(val data: ReviewUIState) : DetailItemUIState(8)
}