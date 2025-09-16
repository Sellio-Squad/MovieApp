package com.karrar.movieapp.di

import com.karrar.movieapp.data.repository.ContentPreferencesRepository
import com.karrar.movieapp.data.repository.ContentPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SingletonRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContentPreferencesRepository(
        impl: ContentPreferencesRepositoryImpl
    ): ContentPreferencesRepository
}
