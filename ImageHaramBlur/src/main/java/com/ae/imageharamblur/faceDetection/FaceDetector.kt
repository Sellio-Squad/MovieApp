package com.ae.imageharamblur.faceDetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

class FaceDetector(private val context: Context) {

    private val interpreterThreadLocal = ThreadLocal<Interpreter?>()
    private val interpreterMutex = Mutex()

    private val modelBuffer by lazy {
        runCatching {
            Log.d(TAG, "Loading model file: $MODEL_FILE")
            FileUtil.loadMappedFile(context, MODEL_FILE)
        }.onSuccess {
            Log.i(TAG, "Model file loaded successfully")
        }.onFailure { e ->
            Log.e(TAG, "Failed to load model file", e)
        }.getOrNull()
    }

    init {
        Log.d(TAG, "Initializing FaceDetector with RFB-320 model")
        setupModel()
    }

    private fun setupModel() {
        runCatching {
            modelBuffer
            Log.d(TAG, "Model setup completed, buffer available: ${modelBuffer != null}")
        }.onFailure { e ->
            Log.e(TAG, "Model setup failed", e)
        }
    }

    private fun getOrCreateInterpreter(): Interpreter? {
        var interpreter = interpreterThreadLocal.get()
        if (interpreter == null && modelBuffer != null) {
            runCatching {
                Log.d(TAG, "Creating new interpreter for thread: ${Thread.currentThread().name}")
                val options = Interpreter.Options().apply {
                    setNumThreads(4)
                    runCatching {
                        setUseXNNPACK(true)
                        Log.d(TAG, "XNNPACK enabled")
                    }.onFailure {
                        Log.w(TAG, "XNNPACK not available")
                    }
                }
                interpreter = Interpreter(modelBuffer!!, options)
                interpreterThreadLocal.set(interpreter)

                Log.i(TAG, "Interpreter created successfully")
            }.onFailure { e ->
                Log.e(TAG, "Failed to create interpreter", e)
                return null
            }
        }
        return interpreter
    }

    suspend fun detectFaces(bitmap: Bitmap): List<DetectedFace> = withContext(Dispatchers.IO) {
        Log.d(TAG, "detectFaces() called - Image size: ${bitmap.width}x${bitmap.height}")

        interpreterMutex.withLock {
            runCatching {
                val interpreter = getOrCreateInterpreter()
                if (interpreter == null) {
                    Log.e(TAG, "No interpreter available, returning empty list")
                    return@withContext emptyList()
                }

                // Scale image to model input size
                val scaledBitmap = bitmap.scale(INPUT_WIDTH, INPUT_HEIGHT)
                val inputBuffer = bitmapToByteBuffer(scaledBitmap)
                scaledBitmap.recycle()

                // Prepare outputs - actual shape is [1, 4420, 4] and [1, 4420, 2]
                val outputBoxes = Array(1) { Array(NUM_BOXES) { FloatArray(4) } }
                val outputScores = Array(1) { Array(NUM_BOXES) { FloatArray(2) } }

                val outputs = mapOf(
                    0 to outputBoxes,
                    1 to outputScores
                )

                // Run inference
                Log.d(TAG, "Running inference...")
                interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)
                Log.d(TAG, "Inference completed")

                // Process detections
                val detections = processResults(
                    outputBoxes[0],
                    outputScores[0],
                    bitmap.width,
                    bitmap.height
                )

                return@withContext detections

            }.getOrElse { e ->
                Log.e(TAG, "Face detection failed", e)
                emptyList()
            }
        }
    }

    private fun processResults(
        boxes: Array<FloatArray>,
        scores: Array<FloatArray>,
        imageWidth: Int,
        imageHeight: Int
    ): List<DetectedFace> {
        val detections = mutableListOf<DetectedFace>()

        Log.d(TAG, "Processing ${boxes.size} potential detections")

        for (i in boxes.indices) {
            // scores[i][1] is the face confidence (index 0 is background)
            val confidence = scores[i][1]

            if (confidence > CONFIDENCE_THRESHOLD) {
                val box = boxes[i]

                // Box format is [y1, x1, y2, x2] normalized to [0, 1]
                val y1 = box[0]
                val x1 = box[1]
                val y2 = box[2]
                val x2 = box[3]

                // Convert to pixel coordinates
                val left = (x1 * imageWidth).toInt().coerceIn(0, imageWidth)
                val top = (y1 * imageHeight).toInt().coerceIn(0, imageHeight)
                val right = (x2 * imageWidth).toInt().coerceIn(0, imageWidth)
                val bottom = (y2 * imageHeight).toInt().coerceIn(0, imageHeight)

                val rect = Rect(left, top, right, bottom)

                // Validate box dimensions
                if (rect.width() >= MIN_FACE_SIZE && rect.height() >= MIN_FACE_SIZE) {
                    detections.add(DetectedFace(rect, confidence))
                    Log.v(TAG, "Added detection: $rect, confidence: $confidence")
                }
            }
        }

        Log.d(TAG, "Found ${detections.size} valid detections before NMS")

        // Apply Non-Maximum Suppression
        return nonMaxSuppression(detections, NMS_THRESHOLD).take(MAX_FACES)
    }

    private fun nonMaxSuppression(
        faces: List<DetectedFace>,
        iouThreshold: Float
    ): List<DetectedFace> {
        if (faces.isEmpty()) return emptyList()

        val sorted = faces.sortedByDescending { it.confidence }
        val selected = mutableListOf<DetectedFace>()

        for (face in sorted) {
            var shouldSelect = true

            for (selectedFace in selected) {
                val iou = calculateIoU(face.boundingBox, selectedFace.boundingBox)
                if (iou > iouThreshold) {
                    shouldSelect = false
                    break
                }
            }

            if (shouldSelect) {
                selected.add(face)
            }
        }

        Log.d(TAG, "NMS: Selected ${selected.size} faces from ${faces.size}")
        return selected
    }

    private fun calculateIoU(box1: Rect, box2: Rect): Float {
        val intersectionLeft = max(box1.left, box2.left)
        val intersectionTop = max(box1.top, box2.top)
        val intersectionRight = min(box1.right, box2.right)
        val intersectionBottom = min(box1.bottom, box2.bottom)

        val intersectionArea = max(0, intersectionRight - intersectionLeft) *
                max(0, intersectionBottom - intersectionTop)

        val box1Area = box1.width() * box1.height()
        val box2Area = box2.width() * box2.height()
        val unionArea = box1Area + box2Area - intersectionArea

        return if (unionArea > 0) intersectionArea.toFloat() / unionArea else 0f
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(1 * INPUT_HEIGHT * INPUT_WIDTH * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_WIDTH * INPUT_HEIGHT)
        bitmap.getPixels(pixels, 0, INPUT_WIDTH, 0, 0, INPUT_WIDTH, INPUT_HEIGHT)

        for (y in 0 until INPUT_HEIGHT) {
            for (x in 0 until INPUT_WIDTH) {
                val pixel = pixels[y * INPUT_WIDTH + x]

                val r = (pixel shr 16 and 0xFF)
                val g = (pixel shr 8 and 0xFF)
                val b = (pixel and 0xFF)

                // Normalize to [-1, 1]
                inputBuffer.putFloat((r - 127.0f) / 128.0f)
                inputBuffer.putFloat((g - 127.0f) / 128.0f)
                inputBuffer.putFloat((b - 127.0f) / 128.0f)
            }
        }

        inputBuffer.rewind()
        return inputBuffer
    }

    fun close() {
        runCatching {
            Log.d(TAG, "Closing FaceDetector")
            interpreterThreadLocal.get()?.close()
            interpreterThreadLocal.remove()
            Log.i(TAG, "FaceDetector closed successfully")
        }.onFailure { e ->
            Log.e(TAG, "Error closing FaceDetector", e)
        }
    }

    companion object {
        private const val TAG = "FaceDetector"
        private const val MODEL_FILE = "version-RFB-320_without_postprocessing.tflite"
        private const val INPUT_WIDTH = 320
        private const val INPUT_HEIGHT = 240
        private const val NUM_BOXES = 4420  // Number of detection boxes
        private const val CONFIDENCE_THRESHOLD = 0.5f
        private const val NMS_THRESHOLD = 0.3f
        private const val MIN_FACE_SIZE = 20
        private const val MAX_FACES = 20
    }
}