package com.karrar.movieapp.data.repository

import com.karrar.movieapp.data.local.AppConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    private val appConfiguration: AppConfiguration
) : LanguageRepository {
    override fun getLanguage(): String? = appConfiguration.getLanguage()
    override fun observeLanguage(): Flow<String?> = flowOf(appConfiguration.getLanguage())
    override suspend fun saveLanguage(code: String) = appConfiguration.saveLanguage(code)
}
