package com.karrar.movieapp.data.repository

import kotlinx.coroutines.flow.Flow

interface ContentPreferencesRepository {
    suspend fun setContentPreference(preference: String)
    fun getContentPreference(): Flow<String>
}