package com.karrar.movieapp.utilities

import androidx.databinding.BindingAdapter
import com.ae.imageharamblur.ui.ImageFilterConfig
import com.ae.imageharamblur.ui.ImageViewFilter

@BindingAdapter(
    value = ["posterImage", "enableModeration", "detectFemales", "detectMales", "forceBlur"],
    requireAll = false
)
fun ImageViewFilter.setFilteredImage(
    imageUrl: String?,
    enableModeration: Boolean = true,
    detectFemales: Boolean = false,
    detectMales: Boolean = false,
    forceBlur: Boolean = false
) {
    this.config = ImageFilterConfig(
        enableModeration = enableModeration,
        detectFemales = detectFemales,
        detectMales = detectMales,
        blurStrength = 80f,
        useContentDetection = true,
        forceBlur = forceBlur
    )

    // Set the image URL
    this.imageModel = imageUrl
}