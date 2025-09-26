package com.karrar.movieapp.di

import com.karrar.movieapp.data.repository.HistoryTipsRepository
import com.karrar.movieapp.data.repository.HistoryTipsRepositoryImpl
import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.data.repository.MovieRepositoryImp
import com.karrar.movieapp.data.repository.SeriesRepository
import com.karrar.movieapp.data.repository.SeriesRepositoryImp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    @ViewModelScoped
    @Binds
    abstract fun bindMovieRepository(
        impl: MovieRepositoryImp
    ): MovieRepository

    @ViewModelScoped
    @Binds
    abstract fun bindSeriesRepository(
        impl: SeriesRepositoryImp
    ): SeriesRepository

    @ViewModelScoped
    @Binds
    abstract fun bindHistoryTipsRepository(
        impl: HistoryTipsRepositoryImpl
    ): HistoryTipsRepository
}
