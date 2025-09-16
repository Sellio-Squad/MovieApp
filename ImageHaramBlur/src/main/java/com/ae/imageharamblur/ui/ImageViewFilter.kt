package com.ae.imageharamblur.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.ae.imageharamblur.R
import com.ae.imageharamblur.databinding.ViewImageFilterBinding
import com.ae.imageharamblur.extensions.toBitmap
import com.ae.imageharamblur.utils.StackBlur
import kotlinx.coroutines.*

class ImageViewFilter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewImageFilterBinding =
        ViewImageFilterBinding.inflate(LayoutInflater.from(context), this, true)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var loadingJob: Job? = null
    private var moderationJob: Job? = null
    private var controller: ImageModerationController? = null

    var config: ImageFilterConfig = ImageFilterConfig()
        set(value) {
            field = value
            if (imageModel != null) {
                loadImage()
            }
        }

    var imageModel: Any? = null
        set(value) {
            field = value
            loadImage()
        }

    var onModerationResult: ((Boolean) -> Unit)? = null
    var onError: ((String?) -> Unit)? = null

    init {
        // Hide all UI elements except imageView
        binding.apply {
            imageView.isVisible = true // Always show image view
            progressBar.isVisible = false
            errorContainer.isVisible = false
            moderatedContainer.isVisible = false
            customLoadingContainer.isVisible = false
            customErrorContainer.isVisible = false
            customModeratedContainer.isVisible = false
            blurTextOverlay.isVisible = false
        }

        // Parse attributes if needed
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.ImageViewFilter) {
                config = ImageFilterConfig(
                    enableModeration = getBoolean(
                        R.styleable.ImageViewFilter_enableModeration,
                        true
                    ),
                    blurStrength = getFloat(R.styleable.ImageViewFilter_blurStrength, 80f),
                    detectFemales = getBoolean(
                        R.styleable.ImageViewFilter_detectFemales,
                        true
                    ),
                    detectMales = getBoolean(R.styleable.ImageViewFilter_detectMales, false),
                    useContentDetection = getBoolean(
                        R.styleable.ImageViewFilter_useContentDetection,
                        true
                    ),
                    showCustomContentWhenBlurred = getBoolean(
                        R.styleable.ImageViewFilter_showCustomContentWhenBlurred,
                        false
                    ),
                    showTextInsteadOfBlur = getBoolean(
                        R.styleable.ImageViewFilter_showTextInsteadOfBlur,
                        false
                    ),
                    forceBlur = getBoolean(
                        R.styleable.ImageViewFilter_forceBlur,
                        false
                    )
                )
            }
        }
    }

    private fun loadImage() {
        // Cancel only the loading job, not moderation
        loadingJob?.cancel()

        loadingJob = coroutineScope.launch {
            try {
                val drawable = loadImageDrawable(context, imageModel)

                if (drawable == null) {
                    onError?.invoke("Failed to load image")
                    return@launch
                }

                // Show image immediately without blur
                val bitmap = drawable.toBitmap() ?: return@launch
                showImage(bitmap, shouldBlur = false)

                // Process moderation in background
                if (config.forceBlur) {
                    showImage(bitmap, shouldBlur = true)
                    onModerationResult?.invoke(true)
                    return@launch
                }

                if (!config.enableModeration) {
                    onModerationResult?.invoke(false)
                    return@launch
                }

                // Cancel previous moderation job
                moderationJob?.cancel()

                // Run moderation in a separate job
                moderationJob = coroutineScope.launch(Dispatchers.Default) {
                    try {
                        // Check if this job is still active
                        if (!isActive) return@launch

                        val imageKey = generateImageKey(imageModel)

                        // Create controller only if job is still active
                        if (!isActive) return@launch

                        // Don't close previous controller if job was cancelled
                        val newController = createModerationController(
                            context = context,
                            imageKey = imageKey,
                            config = config
                        )

                        if (!isActive) {
                            newController.close()
                            return@launch
                        }

                        controller?.close()
                        controller = newController

                        val state = try {
                            controller?.processImage(
                                drawable = drawable,
                                detectFemales = config.detectFemales,
                                detectMales = config.detectMales,
                                useContentDetection = config.useContentDetection
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("ImageViewFilter", "Moderation failed", e)
                            null
                        }

                        // Check if job is still active before updating UI
                        if (!isActive) return@launch

                        state?.let {
                            if (it.shouldBlur && isActive) {
                                withContext(Dispatchers.Main) {
                                    if (isActive) {
                                        showImage(it.originalBitmap!!, shouldBlur = true)
                                        onModerationResult?.invoke(true)
                                    }
                                }
                            } else {
                                onModerationResult?.invoke(false)
                            }
                        }
                    } catch (e: CancellationException) {
                        // Expected when view is recycled
                        android.util.Log.d("ImageViewFilter", "Moderation cancelled")
                    } catch (e: Exception) {
                        android.util.Log.e("ImageViewFilter", "Moderation error", e)
                        onError?.invoke(e.message)
                    }
                }
            } catch (e: CancellationException) {
                // Expected when loading new image
                android.util.Log.d("ImageViewFilter", "Loading cancelled")
            } catch (e: Exception) {
                android.util.Log.e("ImageViewFilter", "Loading error", e)
                onError?.invoke(e.message)
            }
        }
    }

    private fun showImage(bitmap: Bitmap, shouldBlur: Boolean) {
        try {
            binding.imageView.apply {
                if (shouldBlur) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setImageBitmap(bitmap)
                        setRenderEffect(
                            RenderEffect.createBlurEffect(
                                config.blurStrength,
                                config.blurStrength,
                                Shader.TileMode.CLAMP
                            )
                        )
                    } else {
                        try {
                            val blurredBitmap = applyStackBlur(bitmap, config.blurStrength.toInt())
                            setImageBitmap(blurredBitmap)
                        } catch (e: Exception) {
                            // If blur fails, show original
                            android.util.Log.e("ImageViewFilter", "Blur failed", e)
                            setImageBitmap(bitmap)
                        }
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setRenderEffect(null)
                    }
                    setImageBitmap(bitmap)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ImageViewFilter", "Failed to show image", e)
        }
    }

    private fun applyStackBlur(bitmap: Bitmap, radius: Int): Bitmap {
        val output = bitmap.copy(bitmap.config!!, true)
        return StackBlur.process(output, radius.coerceIn(1, 180))
    }

    fun onViewRecycled() {
        // Cancel moderation when view is recycled
        moderationJob?.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Cancel jobs but not the entire scope
        loadingJob?.cancel()
        moderationJob?.cancel()
        controller?.close()
    }

    companion object {
        private fun generateImageKey(model: Any?): String = when (model) {
            is String -> model
            is Int -> model.toString()
            else -> model.hashCode().toString()
        }

        private suspend fun loadImageDrawable(
            context: Context,
            model: Any?
        ): Drawable? = withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context)
                .data(model)
                .allowHardware(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()

            try {
                val result = Coil.imageLoader(context).execute(request)
                result.drawable
            } catch (_: Exception) {
                null
            }
        }

        private fun createModerationController(
            context: Context,
            imageKey: String,
            config: ImageFilterConfig
        ): ImageModerationController = ImageModerationController(
            context = context,
            cacheKey = imageKey,
            enableModeration = config.enableModeration,
            blurStrength = config.blurStrength
        )
    }
}