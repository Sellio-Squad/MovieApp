package com.ae.imageharamblur.ui

import androidx.collection.LruCache

object ModerationCacheManager {
    private const val CACHE_SIZE = 100

    private val cache = LruCache<String, ImageModerationState>(CACHE_SIZE)

    fun get(key: String): ImageModerationState? = cache[key]

    fun put(key: String, state: ImageModerationState) {
        cache.put(key, state)
    }

    fun clear() {
        cache.evictAll()
    }

    fun size(): Int = cache.size()
}
