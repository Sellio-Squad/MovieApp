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

class ImageViewFilter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var moderationJob: Job? = null
    private var controller: ImageModerationController? = null

    var config: ImageFilterConfig = ImageFilterConfig()
        set(value) {
            field = value
            imageModel?.let { loadImage(it) }
        }

    var imageModel: Any? = null
        set(value) {
            field = value
            value?.let { loadImage(it) }
        }

    var onModerationResult: ((Boolean) -> Unit)? = null

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

    private fun loadImage(model: Any) {
        moderationJob?.cancel()

        Coil.imageLoader(context).enqueue(
            ImageRequest.Builder(context)
                .data(model)
                .placeholder(R.drawable.place_holder)
                .error(R.drawable.place_holder)
                .target { drawable ->
                    if (config.forceBlur) {
                        applyBlur(drawable)
                        onModerationResult?.invoke(true)
                    } else if (config.enableModeration) {
                        moderationJob = coroutineScope.launch(Dispatchers.Default) {
                            processModeration(drawable)
                        }
                    } else {
                        setImageDrawable(drawable)
                        onModerationResult?.invoke(false)
                    }
                }
                .build()
        )
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

            withContext(Dispatchers.Main) {
                if (state?.shouldBlur == true) {
                    applyBlur(drawable)
                    onModerationResult?.invoke(true)
                } else {
                    setImageDrawable(drawable)
                    onModerationResult?.invoke(false)
                }
            }
        } catch (_: Exception) { }
    }

    private fun applyBlur(drawable: Drawable) {
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
            val blurred = StackBlur.process(
                bitmap.copy(Bitmap.Config.ARGB_8888, true),
                config.blurStrength.toInt().coerceIn(1, 180)
            )
            setImageBitmap(blurred)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope.cancel()
        controller?.close()
    }
}