package com.karrar.movieapp.data.local


import javax.inject.Inject

interface AppConfiguration {

    fun getSessionId(): String?

    suspend fun saveSessionId(value: String)

    suspend fun saveRequestDate(key: String,value: Long)

    suspend fun getRequestDate(key: String): Long?

    fun getLanguage(): String?

    suspend fun saveLanguage(language: String)

}

class AppConfigurator @Inject constructor(private val dataStorePreferences: DataStorePreferences) :
    AppConfiguration {

    override fun getSessionId(): String? {
        return dataStorePreferences.readString(SESSION_ID_KEY)
    }

    override suspend fun saveSessionId(value: String) {
        dataStorePreferences.writeString(SESSION_ID_KEY, value)
    }

    override suspend fun saveRequestDate(key: String, value: Long) {
        dataStorePreferences.writeLong(key, value)
    }

    override suspend fun getRequestDate(key: String): Long? {
        return dataStorePreferences.readLong(key)
    }

    override fun getLanguage(): String? {
        return dataStorePreferences.readString(LANGUAGE_KEY)
    }

    override suspend fun saveLanguage(language: String) {
        dataStorePreferences.writeString(LANGUAGE_KEY, language)
    }


    companion object DataStorePreferencesKeys {
        const val SESSION_ID_KEY = "session_id"
        const val LANGUAGE_KEY = "language"
    }
}