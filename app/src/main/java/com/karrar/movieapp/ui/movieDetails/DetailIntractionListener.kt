package com.karrar.movieapp.ui.movieDetails

import com.karrar.movieapp.domain.enums.MovieItemsType
import com.karrar.movieapp.domain.enums.TvShowItemsType
import com.karrar.movieapp.ui.base.BaseInteractionListener

interface DetailInteractionListener : BaseInteractionListener {

    fun onclickBack()

    fun onClickSave()

    fun onClickPlayTrailer()

    fun onClickSeeAllMovie(movieItemsType: MovieItemsType)

    fun onClickSeeAllTvShows(tvShowItemsType: TvShowItemsType)

    fun onclickViewReviews()
    fun onClickViewSeasons()
}