package com.karrar.movieapp.data.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.karrar.movieapp.data.local.DataStorePreferences
import javax.inject.Inject

class HistoryTipsRepositoryImpl @Inject constructor(
    private val dataStorePreferences: DataStorePreferences
) : HistoryTipsRepository {

    override suspend fun showHistoryTip(): Boolean {
        return dataStorePreferences.readBoolean(SHOW_HISTORY_TIP.toString()) != false
    }

    override suspend fun closeHistoryTip() {
        dataStorePreferences.writeBoolean(SHOW_HISTORY_TIP.toString(), false)
    }

    private companion object Keys {
        val SHOW_HISTORY_TIP = booleanPreferencesKey("show_history_tip")

    }
}