package com.ae.imageharamblur.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.ae.imageharamblur.R
import com.ae.imageharamblur.databinding.ViewImageFilterBinding
import com.ae.imageharamblur.extensions.toBitmap
import com.ae.imageharamblur.utils.StackBlur
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.withStyledAttributes

class ImageViewFilter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewImageFilterBinding =
        ViewImageFilterBinding.inflate(LayoutInflater.from(context), this, true)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentJob: Job? = null
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
    var onLoadingStateChange: ((Boolean) -> Unit)? = null

    // Custom content providers
    var loadingView: View? = null
    var errorView: View? = null
    var moderatedView: View? = null

    init {

        // Set default views
        loadingView = binding.progressBar
        errorView = binding.errorContainer
        moderatedView = binding.moderatedContainer

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

    fun setLoadingContent(view: View) {
        binding.customLoadingContainer.removeAllViews()
        binding.customLoadingContainer.addView(view)
        loadingView = view
    }

    fun setErrorContent(view: View) {
        binding.customErrorContainer.removeAllViews()
        binding.customErrorContainer.addView(view)
        errorView = view
    }

    fun setModeratedContent(view: View) {
        binding.customModeratedContainer.removeAllViews()
        binding.customModeratedContainer.addView(view)
        moderatedView = view
    }

    private fun loadImage() {
        currentJob?.cancel()
        currentJob = coroutineScope.launch {
            showLoading()

            try {
                val drawable = loadImageDrawable(context, imageModel)

                if (drawable == null) {
                    showError("Failed to load image")
                    return@launch
                }

                android.util.Log.d(
                    "ImageViewFilter",
                    "Moderation enabled: ${config.enableModeration}"
                )
                android.util.Log.d("ImageViewFilter", "Detect females: ${config.detectFemales}")
                android.util.Log.d("ImageViewFilter", "Detect males: ${config.detectMales}")

                // Check for force blur first
                if (config.forceBlur) {
                    android.util.Log.d("ImageViewFilter", "Forcing blur on image")
                    showImage(drawable.toBitmap()!!, shouldBlur = true)
                    onModerationResult?.invoke(true)
                    return@launch
                }


                if (!config.enableModeration) {
                    showImage(drawable.toBitmap()!!, shouldBlur = false)
                    onModerationResult?.invoke(false)
                    return@launch
                }

                val imageKey = generateImageKey(imageModel)
                controller?.close()
                controller = createModerationController(
                    context = context,
                    imageKey = imageKey,
                    config = config
                )

                val state = controller?.processImage(
                    drawable = drawable,
                    detectFemales = config.detectFemales,
                    detectMales = config.detectMales,
                    useContentDetection = config.useContentDetection
                )

                android.util.Log.d("ImageViewFilter", "Should blur: ${state?.shouldBlur}")

                state?.let {
                    if (it.shouldBlur && config.showCustomContentWhenBlurred) {
                        showModerated()
                    } else {
                        showImage(
                            it.originalBitmap!!,
                            shouldBlur = it.shouldBlur && !config.showTextInsteadOfBlur
                        )

                        if (it.shouldBlur && config.showTextInsteadOfBlur) {
                            binding.blurTextOverlay.isVisible = true
                        }
                    }
                    onModerationResult?.invoke(it.shouldBlur)
                }
            } catch (e: Exception) {
                android.util.Log.e("ImageViewFilter", "Error: ${e.message}", e)
                showError(e.message ?: "Unknown error")
            } finally {
                onLoadingStateChange?.invoke(false)
            }
        }
    }

    private fun showLoading() {
        onLoadingStateChange?.invoke(true)
        binding.apply {
            imageView.isVisible = false
            progressBar.isVisible = loadingView == progressBar
            customLoadingContainer.isVisible = loadingView != progressBar
            errorContainer.isVisible = false
            customErrorContainer.isVisible = false
            moderatedContainer.isVisible = false
            customModeratedContainer.isVisible = false
            blurTextOverlay.isVisible = false
        }
    }

    private fun showError(message: String) {
        binding.apply {
            imageView.isVisible = false
            progressBar.isVisible = false
            customLoadingContainer.isVisible = false
            errorText.text = message
            errorContainer.isVisible = errorView == errorContainer
            customErrorContainer.isVisible = errorView != errorContainer
            moderatedContainer.isVisible = false
            customModeratedContainer.isVisible = false
            blurTextOverlay.isVisible = false
        }
    }

    private fun showModerated() {
        binding.apply {
            imageView.isVisible = false
            progressBar.isVisible = false
            customLoadingContainer.isVisible = false
            errorContainer.isVisible = false
            customErrorContainer.isVisible = false
            moderatedContainer.isVisible = moderatedView == moderatedContainer
            customModeratedContainer.isVisible = moderatedView != moderatedContainer
            blurTextOverlay.isVisible = false
        }
    }

    private fun showImage(bitmap: Bitmap, shouldBlur: Boolean) {
        binding.apply {
            imageView.isVisible = true
            progressBar.isVisible = false
            customLoadingContainer.isVisible = false
            errorContainer.isVisible = false
            customErrorContainer.isVisible = false
            moderatedContainer.isVisible = false
            customModeratedContainer.isVisible = false
            blurTextOverlay.isVisible = false

            if (shouldBlur) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    imageView.setImageBitmap(bitmap)
                    applyBlurEffect(imageView, config.blurStrength)
                } else {
                    val blurredBitmap = applyStackBlur(bitmap, config.blurStrength.toInt())
                    imageView.setImageBitmap(blurredBitmap)
                }
            } else {
                imageView.setRenderEffect(null)
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun applyBlurEffect(imageView: ImageView, blurRadius: Float) {
        imageView.setRenderEffect(
            RenderEffect.createBlurEffect(
                blurRadius,
                blurRadius,
                Shader.TileMode.CLAMP
            )
        )
    }

    // Stack blur implementation for pre-Android 12
    private fun applyStackBlur(bitmap: Bitmap, radius: Int): Bitmap {
        val output = bitmap.copy(bitmap.config!!, true)
        return StackBlur.process(output, radius.coerceIn(1, 180))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope.cancel()
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