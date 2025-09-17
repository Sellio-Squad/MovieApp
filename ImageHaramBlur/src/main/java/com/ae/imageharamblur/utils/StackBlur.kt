package com.ae.imageharamblur.utils

import android.graphics.Bitmap
import kotlin.math.min
import androidx.core.graphics.createBitmap

object StackBlur {

    fun process(src: Bitmap, radius: Int): Bitmap {
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)

        src.getPixels(pixels, 0, width, 0, 0, width, height)

        // Apply stack blur algorithm
        fastBlur(pixels, width, height, radius)

        val result = createBitmap(width, height, src.config!!)
        result.setPixels(pixels, 0, width, 0, 0, width, height)

        return result
    }

    private fun fastBlur(pixels: IntArray, width: Int, height: Int, radius: Int) {
        // Simplified fast blur implementation
        // In production, use a proper implementation
        val r = radius.coerceIn(1, 180)

        // This is a placeholder - implement actual blur algorithm
        // You can use RenderScript or a native implementation
    }
}