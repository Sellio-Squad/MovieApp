package com.karrar.movieapp.ui.adapters


import com.karrar.movieapp.R
import com.karrar.movieapp.domain.enums.MovieItemsType
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener
import com.karrar.movieapp.ui.models.MediaUiState

class MovieDetailsAdapter(items: List<MediaUiState>,val listener: MovieDetailsInteractionListener) :
    BaseAdapter<MediaUiState>(items, listener) {
    override val layoutID: Int = R.layout.movie_detail_item
}

interface MovieDetailsInteractionListener : BaseInteractionListener {
    fun onClickSeeAllMovie(movieItemsType: MovieItemsType)

}

