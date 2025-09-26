package com.karrar.movieapp.data.repository

import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    fun getLanguage(): String?
    fun observeLanguage(): Flow<String?>
    suspend fun saveLanguage(code: String)
}