package com.karrar.movieapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class DataStorePreferences(context: Context) {
    private val Context.preferencesDataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(
        PREFERENCES_FILE_NAME
    )
    private val prefDataStore = context.preferencesDataStore

    suspend fun writeLong(key: String, value: Long) {
        prefDataStore.edit { preferences ->
            preferences[longPreferencesKey(key)] = value
        }
    }

    suspend fun readLong(key: String): Long? {
        return prefDataStore.data.firstOrNull()?.get(longPreferencesKey(key))
    }

    suspend fun writeString(key: String, value: String) {
        prefDataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }

    fun readString(key: String): String? {
        return runBlocking { prefDataStore.data.map { it[stringPreferencesKey(key)] }.first() }
    }

    fun readStringFlow(key: String): Flow<String?> =
        prefDataStore.data.map { it[stringPreferencesKey(key)] }

    suspend fun writeBoolean(key: String, value: Boolean) {
        prefDataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }

    suspend fun readBoolean(key: String): Boolean? {
        return prefDataStore.data.firstOrNull()?.get(booleanPreferencesKey(key))
    }

    companion object {
        private const val PREFERENCES_FILE_NAME = "movie"
        const val SHOW_RATING_TIP = "is_rating_history"
        const val CONTENT_PREFERENCE_KEY = "content_preference"
        const val LIST_DETAILS_TIP_SHOWN = "list_details_tip_shown"
    }
}