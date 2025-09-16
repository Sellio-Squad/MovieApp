package com.ae.imageharamblur.ui

data class ImageFilterConfig(
    val enableModeration: Boolean = true,
    val blurStrength: Float = 80f,
    val detectFemales: Boolean = true,
    val detectMales: Boolean = false,
    val useContentDetection: Boolean = true,
    val showCustomContentWhenBlurred: Boolean = false,
    val showTextInsteadOfBlur: Boolean = false,
    val forceBlur: Boolean = false
)