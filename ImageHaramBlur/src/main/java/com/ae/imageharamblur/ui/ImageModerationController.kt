package com.ae.imageharamblur.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import com.ae.imageharamblur.ImageModerationProcessor
import com.ae.imageharamblur.extensions.toBitmap
import com.ae.imageharamblur.utils.blurBitmap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class ImageModerationController(
    context: Context,
    private val cacheKey: String,
    private val enableModeration: Boolean = true,
    private val blurStrength: Float = 80f
) {
    private val processor = if (enableModeration) {
        try {
            ImageModerationProcessor(context)
        } catch (e: Exception) {
            null
        }
    } else null

    private val _state = MutableStateFlow(
        ModerationCacheManager.get(cacheKey) ?: ImageModerationState()
    )
    val state: StateFlow<ImageModerationState> = _state

    suspend fun processImage(
        drawable: Drawable,
        detectFemales: Boolean = true,
        detectMales: Boolean = false,
        useContentDetection: Boolean = true
    ): ImageModerationState = withContext(Dispatchers.Default) {
        // Check cache first
        ModerationCacheManager.get(cacheKey)?.let { cachedState ->
            if (cachedState.isModerated && cachedState.originalBitmap != null) {
                _state.value = cachedState
                return@withContext cachedState
            }
        }

        try {
            // Check if coroutine is still active
            if (!isActive) throw CancellationException()

            val bitmap = drawable.toBitmap()
            if (bitmap == null || bitmap.isRecycled) {
                return@withContext ImageModerationState(
                    isProcessing = false,
                    isModerated = true,
                    shouldBlur = false,
                    originalBitmap = null,
                    error = "Invalid bitmap"
                )
            }

            if (!enableModeration || processor == null) {
                val newState = ImageModerationState(
                    isProcessing = false,
                    isModerated = true,
                    shouldBlur = false,
                    originalBitmap = bitmap
                )
                _state.value = newState
                ModerationCacheManager.put(cacheKey, newState)
                return@withContext newState
            }

            // Check again before processing
            if (!isActive) throw CancellationException()

            val shouldModerate = try {
                processor.shouldModerateImage(
                    bitmap = bitmap,
                    detectFemales = detectFemales,
                    detectMales = detectMales,
                    useContentDetection = useContentDetection
                )
            } catch (e: Exception) {
                false // Don't blur on error
            }

            // Check before creating blurred bitmap
            if (!isActive) throw CancellationException()

            val blurredBitmap = if (shouldModerate && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                try {
                    blurBitmap(bitmap, blurStrength.toInt())
                } catch (e: Exception) {
                    null
                }
            } else null

            val newState = ImageModerationState(
                isProcessing = false,
                isModerated = true,
                shouldBlur = shouldModerate,
                originalBitmap = bitmap,
                blurredBitmap = blurredBitmap
            )

            _state.value = newState
            ModerationCacheManager.put(cacheKey, newState)

            newState
        } catch (e: CancellationException) {
            throw e // Re-throw cancellation
        } catch (e: Exception) {
            // Return safe state on error
            ImageModerationState(
                isProcessing = false,
                isModerated = true,
                shouldBlur = false,
                originalBitmap = drawable.toBitmap(),
                error = e.message
            )
        }
    }

    fun close() {
        try {
            processor?.close()
        } catch (e: Exception) { }
    }
}