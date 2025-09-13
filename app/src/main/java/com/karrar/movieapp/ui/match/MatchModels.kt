package com.karrar.movieapp.ui.match

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.karrar.movieapp.R
import com.karrar.movieapp.domain.models.Media

enum class MatchPages {
    START_PAGE,
    QUESTIONS_PAGE,
    RESULTS_PAGE
}

enum class QuestionType(@StringRes val titleResource: Int) {
    MOOD(R.string.what_mood),
    GENRE(R.string.pick_genre),
    TIME(R.string.how_much_time),
    TYPE(R.string.recent_or_classic)
}

data class MatchUiState(
    val isLoading: Boolean = false,
    val currentPage: MatchPages = MatchPages.START_PAGE,
    val moodQuestions: List<QuestionUiState> = getMoodQuestionAnswers(),
    val genreQuestions: List<QuestionUiState> = getGenreQuestionAnswers(),
    val timeQuestions: List<QuestionUiState> = getTimeQuestionAnswers(),
    val movieTypeQuestions: List<QuestionUiState> = getMovieTypeQuestionAnswers(),
    val currentQuestionType: QuestionType = QuestionType.MOOD,
    val movieGenres: List<GenreUiState> = emptyList(),
    val matchResults: List<Media> = emptyList(),
    val isLoadingRecommendations: Boolean = false,
    val errorMessage: String? = null,
    val shouldShowError: Boolean = false
) {
    val matchProgress: Float =
        currentQuestionType.ordinal.plus(1).toFloat() / QuestionType.entries.size

    val isNextButtonActivated: Boolean = when (currentQuestionType) {
        QuestionType.MOOD -> moodQuestions.any { it.isSelected }
        QuestionType.GENRE -> genreQuestions.any { it.isSelected }
        QuestionType.TIME -> timeQuestions.any { it.isSelected }
        QuestionType.TYPE -> movieTypeQuestions.any { it.isSelected }
    }

    val selectedMoodQuestions: List<QuestionUiState>
        get() = moodQuestions.filter { it.isSelected }

    val selectedGenres: List<QuestionUiState>
        get() = genreQuestions.filter { it.isSelected }

    val selectedTimeQuestion: List<QuestionUiState>
        get() = timeQuestions.filter { it.isSelected }

    val selectedMovieTypeQuestion: List<QuestionUiState>
        get() = movieTypeQuestions.filter { it.isSelected }
}

data class QuestionUiState(
    val id: Int,
    @StringRes val name: Int,
    @StringRes val description: Int? = null,
    val isSelected: Boolean = false,
    @DrawableRes val iconResource: Int? = null,
)

data class GenreUiState(
    val id: Int,
    val name: String
)

data class MatchParams(
    val genres: String?,
    val runtimeGte: Int?,
    val runtimeLte: Int?,
    val releaseDateGte: String?,
    val releaseDateLte: String?
)

fun getMoodQuestionAnswers() = listOf(
    QuestionUiState(
        id = 1,
        name = R.string.mood_chill,
        iconResource = R.drawable.ic_chill
    ),
    QuestionUiState(
        id = 2,
        name = R.string.mood_excited,
        iconResource = R.drawable.ic_excited
    ),
    QuestionUiState(
        id = 3,
        name = R.string.mood_emotional,
        iconResource = R.drawable.ic_emotional
    ),
    QuestionUiState(
        id = 4,
        name = R.string.mood_curious,
        iconResource = R.drawable.ic_curious
    )
)

fun getMovieTypeQuestionAnswers() = listOf(
    QuestionUiState(
        id = 1,
        name = R.string.recent,
    ),
    QuestionUiState(
        id = 2,
        name = R.string.classic,
    ),
    QuestionUiState(
        id = 3,
        name = R.string.both,
    )
)

fun getTimeQuestionAnswers() = listOf(
    QuestionUiState(
        id = 1,
        name = R.string.time_short_label,
        description = R.string.time_short_desc,
        iconResource = R.drawable.ic_time_short
    ),
    QuestionUiState(
        id = 2,
        name = R.string.time_medium_label,
        description = R.string.time_medium_desc,
        iconResource = R.drawable.ic_time_medium
    ),
    QuestionUiState(
        id = 3,
        name = R.string.time_long_label,
        description = R.string.time_long_desc,
        iconResource = R.drawable.ic_time_long
    )
)

fun getGenreQuestionAnswers() = listOf(
    QuestionUiState(id = 1, name = R.string.genre_action),
    QuestionUiState(id = 2, name = R.string.genre_comedy),
    QuestionUiState(id = 3, name = R.string.genre_drama),
    QuestionUiState(id = 4, name = R.string.genre_romance),
    QuestionUiState(id = 5, name = R.string.genre_scifi),
    QuestionUiState(id = 6, name = R.string.genre_thriller),
    QuestionUiState(id = 7, name = R.string.genre_animation),
    QuestionUiState(id = 8, name = R.string.genre_mystery)
)