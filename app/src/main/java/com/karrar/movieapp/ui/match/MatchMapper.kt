package com.karrar.movieapp.ui.match

import com.karrar.movieapp.R

object MatchMapper {

    private val moodToMovieGenres = mapOf(
        R.string.mood_chill to listOf(35, 16, 10751),           // Comedy, Animation, Family
        R.string.mood_excited to listOf(28, 12, 878),           // Action, Adventure, Sci-Fi
        R.string.mood_emotional to listOf(18, 10749),           // Drama, Romance
        R.string.mood_curious to listOf(99, 9648)               // Documentary, Mystery
    )

    private val genreToMovieGenres = mapOf(
        R.string.genre_action to 28,
        R.string.genre_comedy to 35,
        R.string.genre_drama to 18,
        R.string.genre_romance to 10749,
        R.string.genre_scifi to 878,
        R.string.genre_thriller to 53,
        R.string.genre_animation to 16,
        R.string.genre_mystery to 9648
    )

    fun toMatchParams(uiState: MatchUiState): MatchParams {
        val moodGenres = uiState.selectedMoodQuestions
            .flatMap { moodToMovieGenres[it.name] ?: emptyList() }

        val selectedGenres = uiState.selectedGenres
            .mapNotNull { genreToMovieGenres[it.name] }

        val allGenres = (moodGenres + selectedGenres)
            .distinct()
            .joinToString("|")

        var runtimeGte: Int? = null
        var runtimeLte: Int? = null
        uiState.selectedTimeQuestion.firstOrNull()?.name?.let { time ->
            when (time) {
                R.string.time_short_label -> runtimeLte = 90
                R.string.time_medium_label -> {
                    runtimeGte = 90; runtimeLte = 120
                }

                R.string.time_long_label -> runtimeGte = 120
            }
        }

        var releaseDateGte: String? = null
        var releaseDateLte: String? = null
        uiState.selectedMovieTypeQuestion.firstOrNull()?.name?.let { type ->
            when (type) {
                R.string.recent -> releaseDateGte = "2020-01-01"
                R.string.classic -> releaseDateLte = "2000-01-01"
                R.string.both -> {}
            }
        }

        return MatchParams(
            genres = allGenres.ifEmpty { null },
            runtimeGte = runtimeGte,
            runtimeLte = runtimeLte,
            releaseDateGte = releaseDateGte,
            releaseDateLte = releaseDateLte
        )
    }
}