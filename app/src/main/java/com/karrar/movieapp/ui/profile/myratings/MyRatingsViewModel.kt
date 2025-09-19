package com.karrar.movieapp.ui.profile.myratings

import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.data.repository.RatingTipsRepository
import com.karrar.movieapp.domain.models.Genre
import com.karrar.movieapp.domain.models.Rated
import com.karrar.movieapp.domain.usecases.GetGenreListUseCase
import com.karrar.movieapp.domain.usecases.myRatings.GetListOfRatedUseCase
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.ui.search.mediaSearchUIState.MediaTypes
import com.karrar.movieapp.utilities.Constants
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRatingsViewModel @Inject constructor(
    private val getRatedUseCase: GetListOfRatedUseCase,
    private val ratedUIStateMapper: RatedUIStateMapper,
    private val genreUseCase: GetGenreListUseCase,
    private val tipsRepository: RatingTipsRepository,
) : BaseViewModel(), RatedMoviesInteractionListener {

    private val _ratedUiState = MutableStateFlow(MyRateUIState())
    val ratedUiState: StateFlow<MyRateUIState> = _ratedUiState

    private val _myRatingUIEvent: MutableStateFlow<Event<MyRatingUIEvent?>> =
        MutableStateFlow(Event(null))
    val myRatingUIEvent = _myRatingUIEvent.asStateFlow()

    private var movieGenres: List<Genre> = emptyList()
    private var tvGenres: List<Genre> = emptyList()

    init {
        getData()
        getShowTip()
    }

    override fun getData() {
        viewModelScope.launch {
            _ratedUiState.update { it.copy(isLoading = true) }
            if (movieGenres.isEmpty()) {
                movieGenres = genreUseCase(Constants.MOVIE_CATEGORIES_ID)
            }
            if (tvGenres.isEmpty()) {
                tvGenres = genreUseCase(Constants.TV_CATEGORIES_ID)
            }

            try {

                val listOfRated =
                    getRatedUseCase().map { rate ->
                        val genres = getGenresForItem(rate)
                        ratedUIStateMapper.map(rate, genres)
                    }
                _ratedUiState.update { it.copy(ratedList = listOfRated, isLoading = false) }
                _ratedUiState.update {
                    it.copy(
                        currentTypeList = listOfRated.filter { it.mediaType == MediaTypes.MOVIE.toString() },
                        isLoading = false,
                        hasLoaded = true
                    )
                }
            } catch (e: Throwable) {
                _ratedUiState.update {
                    it.copy(
                        error = listOf(Error("")), isLoading = false
                    )
                }
            }
        }
    }

    private fun getGenresForItem(rated: Rated): List<Genre> {
        return if (rated.mediaType == MediaTypes.MOVIE.toString()) {
            movieGenres
        } else {
            tvGenres
        }
    }

    override fun onClickMovie(movieId: Int) {
        ratedUiState.value.ratedList.let { it ->
            val item = it.find { it.id == movieId }
            item?.let {
                if (it.mediaType == Constants.MOVIE) {
                    _myRatingUIEvent.update { Event(MyRatingUIEvent.MovieEvent(movieId)) }
                } else {
                    _myRatingUIEvent.update { Event(MyRatingUIEvent.TVShowEvent(movieId)) }
                }
            }
        }
    }

    fun retryConnect() {
        _ratedUiState.update { it.copy(error = emptyList()) }
        getData()
    }

    fun onTabChanged(contentType: MyRateUIState.ContentType) {
        val mediaType = when (contentType) {
            MyRateUIState.ContentType.MOVIES -> MediaTypes.MOVIE
            MyRateUIState.ContentType.TV_SHOWS -> MediaTypes.TVS_SHOW
        }.toString()
        _ratedUiState.update {
            val filtered = it.ratedList.filter { item -> item.mediaType == mediaType }
            it.copy(
                contentType = contentType,
                currentTypeList = filtered,
            )
        }
    }

    private fun getShowTip() {
        viewModelScope.launch {
            _ratedUiState.update {
                it.copy(
                    showTip = tipsRepository.showRatingTip()
                )
            }
        }
    }

    fun hideTip() {
        viewModelScope.launch {
            tipsRepository.hideRatingTip()
            _ratedUiState.update {
                it.copy(
                    showTip = false
                )
            }
        }
    }

    fun onClickStartRatings() {
        _myRatingUIEvent.update { Event(MyRatingUIEvent.StartRatingsEvent) }
    }
}