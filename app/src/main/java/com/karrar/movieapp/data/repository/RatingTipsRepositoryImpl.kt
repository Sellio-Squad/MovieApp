package com.karrar.movieapp.data.repository

import com.karrar.movieapp.data.local.DataStorePreferences
import com.karrar.movieapp.data.local.DataStorePreferences.Companion.SHOW_RATING_TIP
import jakarta.inject.Inject

class RatingTipsRepositoryImpl @Inject constructor(
    private val dataStore: DataStorePreferences
) : RatingTipsRepository {

    override suspend fun showRatingTip(): Boolean {
        return dataStore.readBoolean(SHOW_RATING_TIP) ?: true
    }

    override suspend fun hideRatingTip() {
        dataStore.writeBoolean(SHOW_RATING_TIP, false)
    }

}