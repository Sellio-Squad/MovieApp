package com.ae.imageharamblur.models

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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

internal class NsfwDetectionModel {

    // Thread-local interpreter instances
    private val interpreterThreadLocal = ThreadLocal<Interpreter?>()
    private val modelBuffer: MappedByteBuffer?
    private val imageProcessor: ImageProcessor
    private val inputImageWidth: Int
    private val inputImageHeight: Int
    private val inputDataType: DataType
    private val outputSize: Int
    private val outputDataType: DataType

    // Mutex for thread-safe access
    private val interpreterLock = Mutex()

    // NSFW categories with proper indices
    enum class Category(val index: Int, val label: String) {
        DRAWING(0, "drawings"),
        HENTAI(1, "hentai"),
        NEUTRAL(2, "neutral"),
        PORN(3, "porn"),
        SEXY(4, "sexy")
    }

    constructor(context: Context) {
        try {
            modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE)

            // Initialize one interpreter to get model info, then close it
            val tempInterpreter = createInterpreter(modelBuffer)
            val modelInfo = extractModelInfo(tempInterpreter)
            tempInterpreter.close()

            this.inputImageHeight = modelInfo.inputHeight
            this.inputImageWidth = modelInfo.inputWidth
            this.inputDataType = modelInfo.inputDataType
            this.outputSize = modelInfo.outputSize
            this.outputDataType = modelInfo.outputDataType

            this.imageProcessor = createImageProcessor()

        } catch (e: Exception) {
            throw Exception("Failed to initialize content detection model: ${e.message}")
        }
    }

    constructor(modelFile: File) {
        try {
            require(modelFile.exists()) { "Model file does not exist: ${modelFile.absolutePath}" }
            require(modelFile.length() > 0) { "Model file is empty" }

            modelBuffer = loadModelFile(modelFile)

            // Initialize one interpreter to get model info, then close it
            val tempInterpreter = createInterpreter(modelBuffer)
            val modelInfo = extractModelInfo(tempInterpreter)
            tempInterpreter.close()

            this.inputImageHeight = modelInfo.inputHeight
            this.inputImageWidth = modelInfo.inputWidth
            this.inputDataType = modelInfo.inputDataType
            this.outputSize = modelInfo.outputSize
            this.outputDataType = modelInfo.outputDataType

            this.imageProcessor = createImageProcessor()

        } catch (e: Exception) {
            throw Exception("Failed to initialize content detection model: ${e.message}")
        }
    }

    private data class ModelInfo(
        val inputHeight: Int,
        val inputWidth: Int,
        val inputDataType: DataType,
        val outputSize: Int,
        val outputDataType: DataType
    )

    private fun extractModelInfo(interpreter: Interpreter): ModelInfo {
        require(interpreter.inputTensorCount > 0) { "Model has no input tensors" }
        require(interpreter.outputTensorCount > 0) { "Model has no output tensors" }

        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape()
        require(inputShape.size >= 3) { "Invalid input shape: ${inputShape.contentToString()}" }

        val outputTensor = interpreter.getOutputTensor(0)
        val outputShape = outputTensor.shape()

        // Verify this is a 5-category model
        val outputSize = outputShape[outputShape.size - 1]
        require(outputSize == 5) { "Model must have 5 output categories, found: $outputSize" }

        return ModelInfo(
            inputHeight = inputShape[1],
            inputWidth = inputShape[2],
            inputDataType = inputTensor.dataType(),
            outputSize = outputSize,
            outputDataType = outputTensor.dataType()
        )
    }

    private fun getOrCreateInterpreter(): Interpreter? {
        var interpreter = interpreterThreadLocal.get()
        if (interpreter == null && modelBuffer != null) {
            interpreter = createInterpreter(modelBuffer)
            interpreterThreadLocal.set(interpreter)
        }
        return interpreter
    }

    private fun loadModelFile(file: File): MappedByteBuffer {
        return FileInputStream(file).use { fileInputStream ->
            fileInputStream.channel.use { fileChannel ->
                fileChannel.map(FileChannel.MapMode.READ_ONLY, 0L, fileChannel.size())
            }
        }
    }

    private fun createInterpreter(modelBuffer: MappedByteBuffer): Interpreter {
        val options = Interpreter.Options().apply {
            setNumThreads(NUM_THREADS)
            // Try to use GPU delegate if available
            try {
                setUseNNAPI(true)
            } catch (e: Exception) {
                setUseNNAPI(false)
            }
        }
        return Interpreter(modelBuffer, options)
    }

    private fun createImageProcessor(): ImageProcessor {
        return ImageProcessor.Builder()
            .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f)) // Normalize to [0, 1]
            .build()
    }

    suspend fun detectContent(bitmap: Bitmap): ContentResult = withContext(Dispatchers.IO) {
        interpreterLock.withLock {
            try {
                val interpreter = getOrCreateInterpreter()
                    ?: return@withContext ContentResult.safe()

                // Validate input
                require(!bitmap.isRecycled) { "Input bitmap is recycled" }
                require(bitmap.width > 0 && bitmap.height > 0) { "Invalid bitmap dimensions" }

                // Process image
                val tensorImage = TensorImage(inputDataType)
                tensorImage.load(bitmap)
                val processedImage = imageProcessor.process(tensorImage)

                // Create output buffer
                val outputBuffer = TensorBuffer.createFixedSize(
                    intArrayOf(1, outputSize),
                    outputDataType
                )

                // Run inference
                interpreter.run(processedImage.buffer, outputBuffer.buffer.rewind())

                // Extract probabilities
                val probabilities = extractProbabilities(outputBuffer)

                // Map to categories
                val categoryScores = mutableMapOf<Category, Float>()
                Category.entries.forEach { category ->
                    categoryScores[category] = probabilities.getOrElse(category.index) { 0f }
                }

                // Calculate inappropriate content score
                val pornScore = categoryScores[Category.PORN] ?: 0f
                val sexyScore = categoryScores[Category.SEXY] ?: 0f
                val hentaiScore = categoryScores[Category.HENTAI] ?: 0f
                val inappropriateScore = pornScore + sexyScore + hentaiScore

                // Determine if content is inappropriate
                val isInappropriate = when {
                    pornScore > STRICT_THRESHOLD -> true
                    hentaiScore > STRICT_THRESHOLD -> true
                    inappropriateScore > DEFAULT_THRESHOLD -> true
                    else -> false
                }


                return@withContext ContentResult(
                    isInappropriate = isInappropriate,
                    inappropriateScore = inappropriateScore,
                    categoryScores = categoryScores,
                    dominantCategory = categoryScores.maxByOrNull { it.value }?.key
                        ?: Category.NEUTRAL,
                    confidence = categoryScores.values.maxOrNull() ?: 0f
                )

            } catch (e: Exception) {
                ContentResult.safe()
            }
        }
    }

    private fun extractProbabilities(outputBuffer: TensorBuffer): FloatArray {
        return when (outputDataType) {
            DataType.FLOAT32 -> {
                val floatArray = FloatArray(outputSize)
                outputBuffer.buffer.rewind()
                outputBuffer.buffer.asFloatBuffer().get(floatArray)
                floatArray
            }

            DataType.UINT8 -> {
                val byteArray = ByteArray(outputSize)
                outputBuffer.buffer.rewind()
                outputBuffer.buffer.get(byteArray)
                FloatArray(outputSize) { i ->
                    (byteArray[i].toInt() and 0xFF) / 255f
                }
            }

            DataType.INT8 -> {
                val byteArray = ByteArray(outputSize)
                outputBuffer.buffer.rewind()
                outputBuffer.buffer.get(byteArray)
                FloatArray(outputSize) { i ->
                    (byteArray[i].toFloat() + 128f) / 255f
                }
            }

            else -> {
                FloatArray(outputSize)
            }
        }
    }

    fun close() {
        try {
            interpreterThreadLocal.get()?.close()
            interpreterThreadLocal.remove()
        } catch (e: Exception) {
        }
    }

    companion object {
        private const val MODEL_FILE = "nsfw_model.tflite"
        private const val DEFAULT_THRESHOLD = 0.3f
        private const val STRICT_THRESHOLD = 0.7f
        private const val NUM_THREADS = 4
    }
}

internal data class ContentResult(
    val isInappropriate: Boolean,
    val inappropriateScore: Float = 0f,
    val categoryScores: Map<NsfwDetectionModel.Category, Float> = emptyMap(),
    val dominantCategory: NsfwDetectionModel.Category = NsfwDetectionModel.Category.NEUTRAL,
    val confidence: Float = 0f
) {
    companion object {
        fun safe() = ContentResult(
            isInappropriate = false,
            inappropriateScore = 0f,
            categoryScores = NsfwDetectionModel.Category.entries.associateWith { 0f },
            dominantCategory = NsfwDetectionModel.Category.NEUTRAL,
            confidence = 1f
        )
    }

}