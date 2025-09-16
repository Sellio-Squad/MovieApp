package com.karrar.movieapp.data.repository

import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    suspend fun changeTheme(theme: String)
    suspend fun getCurrentTheme(): Flow<String>
}