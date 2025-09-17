package com.karrar.movieapp.data.repository

interface HistoryTipsRepository {
    suspend fun showHistoryTip(): Boolean
    suspend fun closeHistoryTip()
}