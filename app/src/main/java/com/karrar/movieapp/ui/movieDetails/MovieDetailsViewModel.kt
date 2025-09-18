package com.karrar.movieapp.ui.movieDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.domain.enums.AllMediaType
import com.karrar.movieapp.domain.enums.MovieItemsType
import com.karrar.movieapp.domain.enums.TvShowItemsType
import com.karrar.movieapp.domain.models.MovieDetails
import com.karrar.movieapp.domain.usecases.GetSessionIDUseCase
import com.karrar.movieapp.domain.usecases.movieDetails.GetMovieDetailsUseCase
import com.karrar.movieapp.domain.usecases.movieDetails.GetMovieRateUseCase
import com.karrar.movieapp.domain.usecases.movieDetails.InsertMoviesUseCase
import com.karrar.movieapp.domain.usecases.movieDetails.SetRatingUseCase
import com.karrar.movieapp.ui.adapters.ActorsInteractionListener
import com.karrar.movieapp.ui.adapters.MovieDetailsInteractionListener
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.ui.mappers.CrewUIStateMapper
import com.karrar.movieapp.ui.movieDetails.mapper.ActorUIStateMapper
import com.karrar.movieapp.ui.movieDetails.mapper.MediaUIStateMapper
import com.karrar.movieapp.ui.movieDetails.mapper.MovieDetailsUIStateMapper
import com.karrar.movieapp.ui.movieDetails.mapper.ReviewUIStateMapper
import com.karrar.movieapp.ui.movieDetails.movieDetailsUIState.DetailItemUIState
import com.karrar.movieapp.ui.movieDetails.movieDetailsUIState.ErrorUIState
import com.karrar.movieapp.ui.movieDetails.movieDetailsUIState.MovieUIState
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
class MovieDetailsViewModel @Inject constructor(
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    private val insertMoviesUseCase: InsertMoviesUseCase,
    private val setRatingUseCase: SetRatingUseCase,
    private val movieDetailsUIStateMapper: MovieDetailsUIStateMapper,
    private val actorUIStateMapper: ActorUIStateMapper,
    private val mediaUIStateMapper: MediaUIStateMapper,
    private val getMovieRate: GetMovieRateUseCase,
    private val reviewUIStateMapper: ReviewUIStateMapper,
    private val sessionIDUseCase: GetSessionIDUseCase,
    private val crewUIStateMapper: CrewUIStateMapper,
    state: SavedStateHandle,
) : BaseViewModel(),MovieDetailsInteractionListener,
    DetailInteractionListener ,ActorsInteractionListener{

    private val args = MovieDetailsFragmentArgs.fromSavedStateHandle(state)

    private val _uiState = MutableStateFlow(MovieUIState())
    val uiState: StateFlow<MovieUIState> = _uiState.asStateFlow()

    private val _movieDetailsUIEvent = MutableStateFlow<Event<MovieDetailsUIEvent?>>(Event(null))
    val movieDetailsUIEvent = _movieDetailsUIEvent.asStateFlow()

    init {
        getData()
    }

    override fun getData() {
        _uiState.update { it.copy(isLoading = true, errorUIStates = emptyList()) }
        getLoginStatus()
        getMovieDetails(args.movieId)
        getMovieCast(args.movieId)
        getSimilarMovie(args.movieId)
        getMovieReviews(args.movieId)
        getMovieCrew(args.movieId)
    }

    private fun getMovieCrew(movieId: Int) {
        viewModelScope.launch {
            try {
                val result = getMovieDetailsUseCase.getMovieCrew(movieId)
                _uiState.update {
                    it.copy(
                        movieCrewResult = result.map { crew -> crewUIStateMapper.map(crew) }
                            .take(8),
                        isLoading = false
                    )
                }
                onAddMovieDetailsItemOfNestedView(
                    DetailItemUIState.Crew(_uiState.value.movieCrewResult)
                )
            } catch (e: Throwable) {
            }
        }
    }

    private fun getMovieDetails(movieId: Int) {
        viewModelScope.launch {
            try {
                val result = getMovieDetailsUseCase.getMovieDetails(movieId)

                _uiState.update {
                    it.copy(
                        movieDetailsResult = movieDetailsUIStateMapper.map(result),
                        isLoading = false,
                    )
                }
                onAddMovieDetailsItemOfNestedView(DetailItemUIState.OverView(_uiState.value.movieDetailsResult))
                addToWatchHistory(result)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorUIStates = listOf(
                            ErrorUIState(
                                code = Constants.INTERNET_STATUS,
                                message = e.message.toString()
                            )
                        ), isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun addToWatchHistory(movie: MovieDetails) {
        insertMoviesUseCase(movie)
    }

    private fun getMovieCast(movieId: Int) {
        viewModelScope.launch {
            try {
                val result = getMovieDetailsUseCase.getMovieCast(movieId)
                _uiState.update {
                    it.copy(
                        movieCastResult = result.map { actor -> actorUIStateMapper.map(actor) },
                        isLoading = false
                    )
                }
                onAddMovieDetailsItemOfNestedView(
                    DetailItemUIState.Cast(_uiState.value.movieCastResult)
                )
            } catch (e: Throwable) {
            }
        }
    }

    private fun getSimilarMovie(movieId: Int) {
        viewModelScope.launch {
            try {
                val result = getMovieDetailsUseCase.getSimilarMovie(movieId)
                _uiState.update {
                    it.copy(
                        similarMoviesResult = result.map { media ->
                            mediaUIStateMapper.map(media)
                        }, isLoading = false
                    )
                }
                onAddMovieDetailsItemOfNestedView(
                    DetailItemUIState.SimilarMovies(_uiState.value.similarMoviesResult)
                )
            } catch (e: Throwable) {
            }
        }
    }

    private fun getLoginStatus() {
        if (!sessionIDUseCase().isNullOrEmpty()) {
            _uiState.update { it.copy(isLogin = true) }
            getRatedMovie(args.movieId)
        }
    }

    private fun getRatedMovie(movieId: Int) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(ratingValue = getMovieRate(movieId)) }
                onAddMovieDetailsItemOfNestedView(DetailItemUIState.Rating(this@MovieDetailsViewModel))
            } catch (e: Throwable) {
            }
        }
    }

    fun onChangeRating(value: Float) {
        viewModelScope.launch {
            try {
                setRatingUseCase(args.movieId, value)
                _uiState.update { it.copy(ratingValue = value) }
                _movieDetailsUIEvent.update { Event(MovieDetailsUIEvent.MessageAppear) }
            } catch (e: Throwable) {
            }
        }
    }

    private fun getMovieReviews(movieId: Int) {
        viewModelScope.launch {
            try {
                val result = getMovieDetailsUseCase.getMovieReviews(movieId)
                _uiState.update {
                    it.copy(
                        movieReview = result.reviews.map { review -> reviewUIStateMapper.map(review) },
                        isLoading = false
                    )
                }
                if (result.reviews.isNotEmpty()) {
                    setReviews(result.isMoreThanMax)
                }
            } catch (e: Throwable) {
            }
        }
    }

    private fun setReviews(showSeeAll: Boolean) {
        _uiState.value.movieReview.forEach {
            onAddMovieDetailsItemOfNestedView(DetailItemUIState.Comment(it))
        }
        onAddMovieDetailsItemOfNestedView(DetailItemUIState.ReviewText)
        if (showSeeAll) {
            onAddMovieDetailsItemOfNestedView(DetailItemUIState.SeeAllReviewsButton)
        }
    }

    private fun onAddMovieDetailsItemOfNestedView(item: DetailItemUIState) {
        val list = _uiState.value.detailItemResult.toMutableList()
        list.add(item)
        _uiState.update { it.copy(detailItemResult = list.toList()) }
    }

    override fun onClickSave() {
        _movieDetailsUIEvent.update { Event(MovieDetailsUIEvent.ClickSaveEvent) }
    }

    override fun onClickPlayTrailer() {
        _movieDetailsUIEvent.update { Event(MovieDetailsUIEvent.ClickPlayTrailerEvent) }
    }

    override fun onclickBack() {
        _movieDetailsUIEvent.update { Event(MovieDetailsUIEvent.ClickBackEvent) }
    }

    override fun onclickViewReviews() {
        _movieDetailsUIEvent.update { Event(MovieDetailsUIEvent.ClickReviewsEvent) }
    }

    override fun onClickSeeAllMovie(movieItemsType: MovieItemsType) {
        val type = when (movieItemsType) {
            MovieItemsType.YOU_MIGHT_ALSO_LIKE -> AllMediaType.YOU_MIGHT_ALSO_LIKE_MOVIES
            MovieItemsType.Top_REVIEWS -> AllMediaType.TOP_REVIEWS_MOVIES
            MovieItemsType.NON -> AllMediaType.ACTOR_MOVIES
        }
        _movieDetailsUIEvent.update { Event(MovieDetailsUIEvent.ClickSeeAllMovieEvent(type)) }
    }

    override fun onClickSeeAllTvShows(tvShowItemsType: TvShowItemsType) {
    }

    override fun onClickActor(actorID: Int) {
    }

}