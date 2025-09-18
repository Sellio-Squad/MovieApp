package com.karrar.movieapp.ui.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.domain.usecases.GetGenreListUseCase
import com.karrar.movieapp.domain.usecases.GetMatchedMoviesUseCase
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val getMatchedMoviesUseCase: GetMatchedMoviesUseCase,
    private val getGenreListUseCase: GetGenreListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableStateFlow<Event<MatchEvent?>>(Event(null))
    val uiEvent = _uiEvent.asStateFlow()

    init {
        loadGenres()
    }

    private fun loadGenres() {
        viewModelScope.launch {
            try {
                val genres =
                    getGenreListUseCase(com.karrar.movieapp.utilities.Constants.MOVIE_CATEGORIES_ID)
                _uiState.value = _uiState.value.copy(
                    movieGenres = genres.map { GenreUiState(it.genreID, it.genreName) }
                )
            } catch (e: Exception) {
            }
        }
    }

    fun onClickStartMatching() {
        _uiState.value = _uiState.value.copy(currentPage = MatchPages.QUESTIONS_PAGE)

    }

    fun onClickNextQuestion() {
        val currentState = _uiState.value ?: return
        if (!currentState.isNextButtonActivated) return

        val nextIndex = currentState.currentQuestionType.ordinal + 1
        val nextQuestionType = QuestionType.entries.getOrNull(nextIndex)

        if (nextQuestionType != null) {
            _uiState.value = currentState.copy(currentQuestionType = nextQuestionType)
        } else {
            loadMatches()
        }
    }

    private fun loadMatches() {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.copy(isLoadingRecommendations = true)

        viewModelScope.launch {
            try {
                val params = MatchMapper.toMatchParams(currentState)
                getMatchedMoviesUseCase(
                    page = 1,
                    genres = params.genres,
                    runtimeGte = params.runtimeGte,
                    runtimeLte = params.runtimeLte,
                    releaseDateGte = params.releaseDateGte,
                    releaseDateLte = params.releaseDateLte
                ).collect { movies ->
                    _uiState.update {
                        it.copy(
                            matchResults = movies.take(10),
                            isLoadingRecommendations = false,
                            currentPage = MatchPages.RESULTS_PAGE
                        )
                    }
                }


            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoadingRecommendations = false,
                    shouldShowError = true,
                    errorMessage = "Failed to load matches. Please try again."
                )
            }
        }
    }

    fun onAnswerSelected(type: QuestionType, answer: QuestionUiState) {
        val currentState = _uiState.value ?: return

        val updatedState = when (type) {
            QuestionType.MOOD -> if (currentState.currentQuestionType == QuestionType.MOOD) {
                currentState.copy(
                    moodQuestions = currentState.moodQuestions.map {
                        if (it.id == answer.id) it.copy(isSelected = !it.isSelected) else it
                    }
                )
            } else currentState

            QuestionType.GENRE -> if (currentState.currentQuestionType == QuestionType.GENRE) {
                currentState.copy(
                    genreQuestions = currentState.genreQuestions.map {
                        if (it.id == answer.id) it.copy(isSelected = !it.isSelected) else it
                    }
                )
            } else currentState

            QuestionType.TIME -> if (currentState.currentQuestionType == QuestionType.TIME) {
                currentState.copy(
                    timeQuestions = currentState.timeQuestions.map {
                        if (it.id == answer.id) it.copy(isSelected = !it.isSelected) else
                            it.copy(isSelected = false)
                    }
                )
            } else currentState

            QuestionType.TYPE -> if (currentState.currentQuestionType == QuestionType.TYPE) {
                currentState.copy(
                    movieTypeQuestions = currentState.movieTypeQuestions.map {
                        if (it.id == answer.id) it.copy(isSelected = !it.isSelected) else
                            it.copy(isSelected = false)
                    }
                )
            } else currentState
        }

        _uiState.value = updatedState
    }

    fun onNavigateBack() {
        val currentState = _uiState.value ?: return

        when (currentState.currentPage) {
            MatchPages.QUESTIONS_PAGE -> {
                val previousIndex = currentState.currentQuestionType.ordinal - 1
                if (previousIndex >= 0) {
                    _uiState.value = currentState.copy(
                        currentQuestionType = QuestionType.entries[previousIndex]
                    )
                } else {
                    _uiState.value = currentState.copy(currentPage = MatchPages.START_PAGE)
                }
            }

            MatchPages.RESULTS_PAGE -> {
                _uiState.value = currentState.copy(
                    currentPage = MatchPages.START_PAGE,
                    currentQuestionType = QuestionType.MOOD,
                    moodQuestions = getMoodQuestionAnswers(),
                    genreQuestions = getGenreQuestionAnswers(),
                    timeQuestions = getTimeQuestionAnswers(),
                    movieTypeQuestions = getMovieTypeQuestionAnswers()
                )
            }

            else -> {}
        }
    }

    fun onMovieClick(movieId: Int) {
        _uiEvent.value = Event(MatchEvent.OnMovieClick(movieId))
    }

    fun onRetry() {
        _uiState.value = _uiState.value.copy(
            shouldShowError = false,
            errorMessage = null
        )
        loadMatches()
    }

    fun resetEvent() {
        _uiEvent.value = Event(null)
    }
}

sealed class MatchEvent {
    data class OnMovieClick(val id: Int) : MatchEvent()
}