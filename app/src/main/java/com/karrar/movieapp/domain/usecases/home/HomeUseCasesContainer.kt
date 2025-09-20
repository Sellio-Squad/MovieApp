package com.karrar.movieapp.domain.usecases.home

import com.karrar.movieapp.domain.usecase.home.getData.GetTrendingActorsUseCase
import com.karrar.movieapp.domain.usecase.home.getData.movie.GetAdventureMoviesUseCase
import com.karrar.movieapp.domain.usecase.home.getData.movie.GetMysteryMoviesUseCase
import com.karrar.movieapp.domain.usecase.home.getData.movie.GetNowStreamingMoviesUseCase
import com.karrar.movieapp.domain.usecase.home.getData.movie.GetPopularMoviesUseCase
import com.karrar.movieapp.domain.usecase.home.getData.movie.GetTrendingMoviesUseCase
import com.karrar.movieapp.domain.usecase.home.getData.movie.GetUpcomingMoviesUseCase
import com.karrar.movieapp.domain.usecase.home.getData.series.GetAiringTodaySeriesUseCase
import com.karrar.movieapp.domain.usecase.home.getData.series.GetOnTheAirSeriesUseCase
import com.karrar.movieapp.domain.usecase.home.getData.series.GetTopRatedTvShowSeriesUseCase
import com.karrar.movieapp.domain.usecases.CheckIfLoggedInUseCase
import com.karrar.movieapp.domain.usecases.GetAccountDetailsUseCase
import com.karrar.movieapp.domain.usecases.home.getData.movie.GetMatchesYourVibeMoviesUseCase
import com.karrar.movieapp.domain.usecases.myHistory.GetWatchHistoryUseCase
import com.karrar.movieapp.domain.usecases.mylist.GetMyListUseCase
import javax.inject.Inject

class HomeUseCasesContainer @Inject constructor(
    val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    val getAiringTodayUseCase: GetAiringTodaySeriesUseCase,
    val getOnTheAirUseCase: GetOnTheAirSeriesUseCase,
    val getTopRatedTvShowUseCase: GetTopRatedTvShowSeriesUseCase,
    val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase,
    val getMysteryMoviesUseCase: GetMysteryMoviesUseCase,
    val getAdventureMoviesUseCase: GetAdventureMoviesUseCase,
    val getNowStreamingMoviesUseCase: GetNowStreamingMoviesUseCase,
    val getTrendingActorsUseCase: GetTrendingActorsUseCase,
    val getWatchHistoryUseCase: GetWatchHistoryUseCase,
    val getMyListUseCase: GetMyListUseCase,
    val getAccountDetailsUseCase: GetAccountDetailsUseCase,
    val checkIfLoggedInUseCase: CheckIfLoggedInUseCase,
    val getMatchesYourVibeMoviesUseCase: GetMatchesYourVibeMoviesUseCase
)