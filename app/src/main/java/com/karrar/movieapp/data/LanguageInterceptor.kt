package com.karrar.movieapp.data

import okhttp3.Interceptor
import okhttp3.Response

class LanguageInterceptor(
    private val languageProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val languageTag = resolveLanguageTag(languageProvider())

        val newUrl = request.url.newBuilder()
            .addQueryParameter("language", languageTag)
            .build()

        val updated = request.newBuilder().url(newUrl).build()
        return chain.proceed(updated)
    }

    private fun resolveLanguageTag(code: String?): String {
        return when (code?.lowercase()) {
            "ar", "ar-sa" -> "ar-SA"
            "en", "en-us" -> "en-US"
            else -> "en-US"
        }
    }
}
