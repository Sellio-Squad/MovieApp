package com.karrar.movieapp.utilities

import androidx.databinding.BindingAdapter
import com.ae.imageharamblur.ui.ImageFilterConfig
import com.ae.imageharamblur.ui.ImageViewFilter
import com.karrar.movieapp.ui.profile.settings.contentPreferences.ContentPreferencesTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object ImageFilterBindingAdapters {

    private var globalContentPreferencesManager: ContentPreferencesManager? = null

    fun initialize(manager: ContentPreferencesManager) {
        globalContentPreferencesManager = manager
    }

    @JvmStatic
    @BindingAdapter("app:mediaPoster")
    fun ImageViewFilter.setMediaPoster(imageUrl: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            val preference = globalContentPreferencesManager?.contentPreference?.first()
                ?: ContentPreferencesTypes.ShowAll

            val config = when (preference) {
                ContentPreferencesTypes.Strict -> ImageFilterConfig(
                    enableModeration = true,
                    detectFemales = true,
                    useContentDetection = true,
                    blurStrength = 80f
                )
                ContentPreferencesTypes.HideExplicit -> ImageFilterConfig(
                    enableModeration = true,
                    detectFemales = false,
                    useContentDetection = true,
                    blurStrength = 60f
                )
                ContentPreferencesTypes.ShowAll -> ImageFilterConfig(
                    enableModeration = false
                )
            }

            this@setMediaPoster.config = config
            this@setMediaPoster.imageModel = imageUrl
        }
    }

    // Also add for other image types
    @JvmStatic
    @BindingAdapter("app:posterImage")
    fun ImageViewFilter.setPosterImage(imageUrl: String?) {
        setMediaPoster(imageUrl)
    }
}