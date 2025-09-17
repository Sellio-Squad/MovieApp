package com.ae.imageharamblur.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import com.ae.imageharamblur.R
import com.ae.imageharamblur.utils.StackBlur
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

class ImageViewFilter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

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

        clipToOutline = true

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.ImageViewFilter) {
                config = ImageFilterConfig(
                    enableModeration = getBoolean(R.styleable.ImageViewFilter_enableModeration, true),
                    blurStrength = getFloat(R.styleable.ImageViewFilter_blurStrength, 80f),
                    detectFemales = getBoolean(R.styleable.ImageViewFilter_detectFemales, true),
                    detectMales = getBoolean(R.styleable.ImageViewFilter_detectMales, false),
                    useContentDetection = getBoolean(R.styleable.ImageViewFilter_useContentDetection, true),
                    forceBlur = getBoolean(R.styleable.ImageViewFilter_forceBlur, false)
                )
            }
        }
    }

    private fun loadImage() {
        loadingJob?.cancel()

        loadingJob = coroutineScope.launch {
            try {
                val drawable = loadImageDrawable(context, imageModel) ?: return@launch

                showImage(drawable, shouldBlur = false)

                if (config.forceBlur) {
                    showImage(drawable, shouldBlur = true)
                    onModerationResult?.invoke(true)
                } else if (config.enableModeration) {
                    moderationJob?.cancel()
                    moderationJob = launch(Dispatchers.Default) {
                        processModeration(drawable)
                    }
                } else {
                    onModerationResult?.invoke(false)
                }
            } catch (e: Exception) {
                onError?.invoke(e.message)
            }
        }
    }

    private suspend fun processModeration(drawable: Drawable) {
        try {
            val imageKey = imageModel?.toString() ?: ""
            controller?.close()
            controller = ImageModerationController(
                context = context,
                cacheKey = imageKey,
                enableModeration = config.enableModeration,
                blurStrength = config.blurStrength
            )

            val state = controller?.processImage(
                drawable = drawable,
                detectFemales = config.detectFemales,
                detectMales = config.detectMales,
                useContentDetection = config.useContentDetection
            )

            if (state?.shouldBlur == true && coroutineContext.isActive) {
                withContext(Dispatchers.Main) {
                    showImage(drawable, shouldBlur = true)
                    onModerationResult?.invoke(true)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onModerationResult?.invoke(false)
                }
            }
        } catch (e: Exception) { }
    }

    private fun showImage(drawable: Drawable, shouldBlur: Boolean) {
        if (shouldBlur) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setImageDrawable(drawable)
                setRenderEffect(
                    RenderEffect.createBlurEffect(
                        config.blurStrength,
                        config.blurStrength,
                        Shader.TileMode.CLAMP
                    )
                )
            } else {
                val bitmap = drawable.toBitmap()
                val blurredBitmap = StackBlur.process(
                    bitmap.copy(Bitmap.Config.ARGB_8888, true),
                    config.blurStrength.toInt().coerceIn(1, 180)
                )
                setImageBitmap(blurredBitmap)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRenderEffect(null)
            }
            setImageDrawable(drawable)
        }
    }

    private suspend fun loadImageDrawable(context: Context, model: Any?): Drawable? {
        return withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context)
                .data(model)
                .allowHardware(false)
                .build()

            try {
                Coil.imageLoader(context).execute(request).drawable
            } catch (_: Exception) {
                null
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope.cancel()
        controller?.close()
    }
}