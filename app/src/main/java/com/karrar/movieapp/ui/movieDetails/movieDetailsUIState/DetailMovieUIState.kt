package com.karrar.movieapp.ui.movieDetails.movieDetailsUIState

import androidx.lifecycle.ViewModel
import com.karrar.movieapp.ui.models.ActorUiState
import com.karrar.movieapp.ui.models.CrewUIState
import com.karrar.movieapp.ui.models.MediaUiState

sealed class DetailMovieUIState(val priority: Int) {
    class OverView(val data: MovieDetailsUIState) : DetailMovieUIState(0)

    class Cast(val data: List<ActorUiState>) : DetailMovieUIState(1)

    class Crew(val data: List<CrewUIState>) : DetailMovieUIState(2)
    class SimilarMovies(val data: List<MediaUiState>) : DetailMovieUIState(3)

    class Comment(val data: ReviewUIState) : DetailMovieUIState(6)

    class Rating(val viewModel: ViewModel) : DetailMovieUIState(4)

    object ReviewText : DetailMovieUIState(5)

}