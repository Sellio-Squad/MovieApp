package com.ae.imageharamblur.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.createBitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class GenderDetectionModel {
    private var interpreter: Interpreter? = null
    private val imageProcessor: ImageProcessor
    private val inputSize: Int
    private val inputDataType: DataType
    private val outputShape: IntArray
    private val outputDataType: DataType
    private val interpreterLock = ReentrantLock()

    @Volatile
    private var isInitialized = false

    @Volatile
    private var isClosed = false

    constructor(context: Context) {
        try {
            val modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE)

            // Initialize model properties first
            val tempInterpreter = createInterpreter(modelBuffer)
            val (inputProps, outputProps) = initializeModelProperties(tempInterpreter)

            this.inputSize = inputProps.first
            this.inputDataType = inputProps.second
            this.outputShape = outputProps.first
            this.outputDataType = outputProps.second

            // Create processors
            this.imageProcessor = createImageProcessor()

            // Set interpreter last
            this.interpreter = tempInterpreter
            this.isInitialized = true

        } catch (e: Exception) {
            close()
            throw RuntimeException("Failed to initialize gender detection model", e)
        }
    }

    constructor(modelFile: File) {
        try {
            val modelBuffer = loadModelFile(modelFile)

            // Initialize model properties first
            val tempInterpreter = createInterpreter(modelBuffer)
            val (inputProps, outputProps) = initializeModelProperties(tempInterpreter)

            this.inputSize = inputProps.first
            this.inputDataType = inputProps.second
            this.outputShape = outputProps.first
            this.outputDataType = outputProps.second

            // Create processors
            this.imageProcessor = createImageProcessor()

            // Set interpreter last
            this.interpreter = tempInterpreter
            this.isInitialized = true

        } catch (e: Exception) {
            close()
            throw RuntimeException("Failed to initialize gender detection model from file", e)
        }
    }

    private fun initializeModelProperties(tempInterpreter: Interpreter):
            Pair<Pair<Int, DataType>, Pair<IntArray, DataType>> {
        val inputTensor = tempInterpreter.getInputTensor(0)
        val outputTensor = tempInterpreter.getOutputTensor(0)

        val inputShape = inputTensor.shape()
        val inputSize = when {
            inputShape.size >= 4 -> inputShape[1] // NHWC format
            inputShape.size >= 3 -> inputShape[1] // HWC format
            else -> INPUT_SIZE
        }

        return Pair(
            Pair(inputSize, inputTensor.dataType()),
            Pair(outputTensor.shape().clone(), outputTensor.dataType())
        )
    }

    private fun loadModelFile(file: File): MappedByteBuffer {
        FileInputStream(file).use { fileInputStream ->
            fileInputStream.channel.use { fileChannel ->
                val startOffset = 0L
                val declaredLength = fileChannel.size()
                return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            }
        }
    }

    private fun createInterpreter(modelBuffer: MappedByteBuffer): Interpreter {
        val options = Interpreter.Options().apply {
            setNumThreads(2) // Reduce threads to avoid concurrency issues
            // Don't use NNAPI or GPU delegates - they can cause crashes
            setUseNNAPI(false)
            try {
                // XNNPack is generally stable
                setUseXNNPACK(true)
            } catch (e: Exception) {
                // Ignore if not available
            }
        }
        return Interpreter(modelBuffer, options)
    }

    private fun createImageProcessor(): ImageProcessor {
        return ImageProcessor.Builder()
            .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(IMAGE_MEAN, IMAGE_STD))
            .build()
    }

    fun detectGender(faceBitmap: Bitmap): GenderResult {
        // Fast fail if closed
        if (isClosed || !isInitialized || interpreter == null) {
            return GenderResult(isFemale = false, confidence = 0.5f)
        }

        return interpreterLock.withLock {
            // Double-check after acquiring lock
            if (isClosed || !isInitialized || interpreter == null) {
                return@withLock GenderResult(isFemale = false, confidence = 0.5f)
            }

            detectGenderInternal(faceBitmap)
        }
    }

    private fun detectGenderInternal(faceBitmap: Bitmap): GenderResult {
        return try {
            // Validate input
            if (faceBitmap.isRecycled || faceBitmap.width <= 0 || faceBitmap.height <= 0) {
                return GenderResult(isFemale = false, confidence = 0.5f)
            }

            // Create output buffer for this inference
            val outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType)

            // Prepare input
            val rgbBitmap = ensureRgbBitmap(faceBitmap)
            val tensorImage = TensorImage(inputDataType)
            tensorImage.load(rgbBitmap)

            // Process image
            val processedImage = imageProcessor.process(tensorImage)

            // Run inference - check if still valid
            val currentInterpreter = interpreter
            if (currentInterpreter == null || isClosed) {
                return GenderResult(isFemale = false, confidence = 0.5f)
            }

            currentInterpreter.run(processedImage.buffer, outputBuffer.buffer)

            // Process results
            val probabilities = extractProbabilities(outputBuffer)
            val normalizedProbabilities = normalizeProbabilities(probabilities)

            createGenderResult(normalizedProbabilities)

        } catch (e: IllegalStateException) {
            // Interpreter was closed during execution
            GenderResult(isFemale = false, confidence = 0.5f)
        } catch (e: Exception) {
            // Any other error
            GenderResult(isFemale = false, confidence = 0.5f)
        }
    }

    private fun extractProbabilities(outputBuffer: TensorBuffer): FloatArray {
        return try {
            val buffer = outputBuffer.buffer
            buffer.rewind()

            when (outputBuffer.dataType) {
                DataType.FLOAT32 -> {
                    val floats = FloatArray(buffer.remaining() / 4)
                    buffer.asFloatBuffer().get(floats)
                    floats
                }
                DataType.UINT8 -> {
                    val size = buffer.remaining()
                    val byteArray = ByteArray(size)
                    buffer.get(byteArray)
                    FloatArray(size) { i ->
                        (byteArray[i].toInt() and 0xFF) / 255.0f
                    }
                }
                else -> floatArrayOf(0.5f, 0.5f)
            }
        } catch (e: Exception) {
            floatArrayOf(0.5f, 0.5f)
        }
    }

    private fun normalizeProbabilities(probabilities: FloatArray): Pair<Float, Float> {
        if (probabilities.size < 2) {
            return 0.5f to 0.5f
        }

        val femaleProbability = probabilities[FEMALE_INDEX].coerceIn(0f, 1f)
        val maleProbability = probabilities[MALE_INDEX].coerceIn(0f, 1f)

        val sum = femaleProbability + maleProbability
        return when {
            sum in 0.95f..1.05f -> femaleProbability to maleProbability
            sum > 0 -> (femaleProbability / sum) to (maleProbability / sum)
            else -> 0.5f to 0.5f
        }
    }

    private fun createGenderResult(normalizedProbabilities: Pair<Float, Float>): GenderResult {
        val (femaleProbability, maleProbability) = normalizedProbabilities
        val isFemale = femaleProbability > maleProbability
        val confidence = if (isFemale) femaleProbability else maleProbability

        return GenderResult(
            isFemale = isFemale,
            confidence = confidence.coerceIn(0f, 1f)
        )
    }

    private fun ensureRgbBitmap(bitmap: Bitmap): Bitmap {
        return if (bitmap.config == Bitmap.Config.ARGB_8888) {
            bitmap
        } else {
            createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888).apply {
                Canvas(this).drawBitmap(bitmap, 0f, 0f, null)
            }
        }
    }

    fun close() {
        interpreterLock.withLock {
            isClosed = true
            isInitialized = false

            try {
                interpreter?.close()
            } catch (e: Exception) {
                // Ignore errors during cleanup
            } finally {
                interpreter = null
            }
        }
    }

    companion object {
        private const val MODEL_FILE = "GenderClass_06_03-20-08.tflite"
        private const val INPUT_SIZE = 224
        private const val IMAGE_MEAN = 127.5f
        private const val IMAGE_STD = 127.5f
        private const val FEMALE_INDEX = 0
        private const val MALE_INDEX = 1
        private const val NUM_THREADS = 2
    }
}

internal data class GenderResult(
    val isFemale: Boolean,
    val confidence: Float
)