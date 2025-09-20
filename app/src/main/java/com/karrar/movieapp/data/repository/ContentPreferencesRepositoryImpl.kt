package com.karrar.movieapp.data.repository

import com.karrar.movieapp.data.local.DataStorePreferences
import com.karrar.movieapp.data.local.DataStorePreferences.Companion.CONTENT_PREFERENCE_KEY
import com.karrar.movieapp.ui.profile.settings.contentPreferences.ContentPreferencesTypes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContentPreferencesRepositoryImpl @Inject constructor(
    private val dataStorePreferences: DataStorePreferences
) : ContentPreferencesRepository {

    override suspend fun setContentPreference(preference: String) {
        dataStorePreferences.writeString(CONTENT_PREFERENCE_KEY, preference)
    }

    override fun getContentPreference(): Flow<String> =
        dataStorePreferences.readStringFlow(CONTENT_PREFERENCE_KEY)
            .map { it ?: ContentPreferencesTypes.HideExplicit.name }
}