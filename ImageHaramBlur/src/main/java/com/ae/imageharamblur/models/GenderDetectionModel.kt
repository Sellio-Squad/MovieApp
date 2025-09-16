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

internal class GenderDetectionModel {
    private val interpreter: Interpreter
    private val imageProcessor: ImageProcessor
    private val inputSize: Int
    private val inputDataType: DataType

    constructor(context: Context) {
        try {
            val modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE)

            this.interpreter = createInterpreter(modelBuffer)

            val inputTensor = interpreter.getInputTensor(0)
            val inputShape = inputTensor.shape()
            this.inputSize = if (inputShape.size >= 3) inputShape[1] else INPUT_SIZE
            this.inputDataType = inputTensor.dataType()

            this.imageProcessor = createImageProcessor()

        } catch (e: Exception) {
            throw e
        }
    }

    constructor(modelFile: File) {
        try {
            val modelBuffer = loadModelFile(modelFile)

            this.interpreter = createInterpreter(modelBuffer)

            val inputTensor = interpreter.getInputTensor(0)
            val inputShape = inputTensor.shape()
            this.inputSize = if (inputShape.size >= 3) inputShape[1] else INPUT_SIZE
            this.inputDataType = inputTensor.dataType()

            this.imageProcessor = createImageProcessor()

        } catch (e: Exception) {
            throw e
        }
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
            setNumThreads(NUM_THREADS)
            // Enable GPU delegate if available
            try {
                setUseNNAPI(true)
            } catch (e: Exception) {
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
        return try {
            // Validate input
            require(!faceBitmap.isRecycled) { "Input bitmap is recycled" }
            require(faceBitmap.width > 0 && faceBitmap.height > 0) { "Invalid bitmap dimensions" }

            // Prepare input
            val rgbBitmap = ensureRgbBitmap(faceBitmap)
            val tensorImage = TensorImage(inputDataType)
            tensorImage.load(rgbBitmap)

            // Process image
            val processedImage = imageProcessor.process(tensorImage)

            // Run inference
            val outputBuffer = TensorBuffer.createFixedSize(
                interpreter.getOutputTensor(0).shape(),
                interpreter.getOutputTensor(0).dataType()
            )

            synchronized(interpreter) {
                interpreter.run(processedImage.buffer, outputBuffer.buffer.rewind())
            }

            // Process results
            val probabilities = extractProbabilities(outputBuffer)
            val normalizedProbabilities = normalizeProbabilities(probabilities)

            createGenderResult(normalizedProbabilities)

        } catch (e: Exception) {
            // Return uncertain result on error
            GenderResult(isFemale = false, confidence = 0.5f)
        }
    }

    private fun extractProbabilities(outputBuffer: TensorBuffer): FloatArray {
        return when (outputBuffer.dataType) {
            DataType.FLOAT32 -> outputBuffer.floatArray
            DataType.UINT8 -> {
                val byteArray = ByteArray(outputBuffer.buffer.remaining())
                outputBuffer.buffer.get(byteArray)
                FloatArray(byteArray.size) { i ->
                    (byteArray[i].toInt() and 0xFF) / 255.0f
                }
            }

            else -> {
                floatArrayOf(0.5f, 0.5f)
            }
        }
    }

    private fun normalizeProbabilities(probabilities: FloatArray): Pair<Float, Float> {
        val femaleProbability = probabilities.getOrElse(FEMALE_INDEX) { 0.5f }
        val maleProbability = probabilities.getOrElse(MALE_INDEX) { 0.5f }

        // Check if already normalized (sum ≈ 1)
        val sum = femaleProbability + maleProbability
        return if (sum in 0.95f..1.05f) {
            femaleProbability to maleProbability
        } else {
            // Apply softmax normalization
            val maxProb = maxOf(femaleProbability, maleProbability)
            val expFemale = kotlin.math.exp(femaleProbability - maxProb)
            val expMale = kotlin.math.exp(maleProbability - maxProb)
            val sumExp = expFemale + expMale

            (expFemale / sumExp).toFloat() to (expMale / sumExp).toFloat()
        }
    }

    private fun createGenderResult(normalizedProbabilities: Pair<Float, Float>): GenderResult {
        val (femaleProbability, maleProbability) = normalizedProbabilities
        val isFemale = femaleProbability > maleProbability
        val confidence = if (isFemale) femaleProbability else maleProbability

        return GenderResult(isFemale = isFemale, confidence = confidence)
    }

    private fun ensureRgbBitmap(bitmap: Bitmap): Bitmap {
        return if (bitmap.config == Bitmap.Config.ARGB_8888) {
            createBitmap(bitmap.width, bitmap.height).apply {
                Canvas(this).drawBitmap(bitmap, 0f, 0f, null)
            }
        } else {
            bitmap
        }
    }

    fun close() {
        try {
            interpreter.close()
        } catch (e: Exception) {
        }
    }

    companion object {
        private const val MODEL_FILE = "GenderClass_06_03-20-08.tflite"
        private const val INPUT_SIZE = 224
        private const val IMAGE_MEAN = 127.5f
        private const val IMAGE_STD = 127.5f
        private const val FEMALE_INDEX = 0
        private const val MALE_INDEX = 1
        private const val NUM_THREADS = 4
    }
}

internal data class GenderResult(
    val isFemale: Boolean,
    val confidence: Float
)