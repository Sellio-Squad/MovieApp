package com.ae.imageharamblur.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
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
import kotlin.math.exp

internal class GenderDetectionModel {
    private val interpreter: Interpreter
    private val imageProcessor: ImageProcessor
    private val inputSize: Int
    private val inputDataType: DataType

    constructor(context: Context) {
        Log.d(TAG, "Initializing GenderDetectionModel with context")
        val modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE)
        Log.i(TAG, "Model file loaded from assets: $MODEL_FILE")

        this.interpreter = createInterpreter(modelBuffer)

        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape()
        this.inputSize = if (inputShape.size >= 3) inputShape[1] else INPUT_SIZE
        this.inputDataType = inputTensor.dataType()

        Log.d(TAG, "Input configuration - Size: $inputSize, DataType: $inputDataType, Shape: ${inputShape.contentToString()}")

        this.imageProcessor = createImageProcessor()
        Log.i(TAG, "GenderDetectionModel initialized successfully")
    }

    constructor(modelFile: File) {
        Log.d(TAG, "Initializing GenderDetectionModel with file: ${modelFile.absolutePath}")
        val modelBuffer = loadModelFile(modelFile)
        Log.i(TAG, "Model file loaded from: ${modelFile.name}")

        this.interpreter = createInterpreter(modelBuffer)

        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape()
        this.inputSize = if (inputShape.size >= 3) inputShape[1] else INPUT_SIZE
        this.inputDataType = inputTensor.dataType()

        Log.d(TAG, "Input configuration - Size: $inputSize, DataType: $inputDataType, Shape: ${inputShape.contentToString()}")

        this.imageProcessor = createImageProcessor()
        Log.i(TAG, "GenderDetectionModel initialized successfully")
    }

    private fun loadModelFile(file: File): MappedByteBuffer {
        Log.d(TAG, "Loading model file: ${file.absolutePath}")
        return try {
            val fileInputStream = FileInputStream(file)
            val fileChannel = fileInputStream.channel
            val startOffset = 0L
            val declaredLength = fileChannel.size()
            Log.d(TAG, "Model file size: $declaredLength bytes")
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model file", e)
            throw e
        }
    }

    private fun createInterpreter(modelBuffer: MappedByteBuffer): Interpreter {
        Log.d(TAG, "Creating interpreter")
        return try {
            val options = Interpreter.Options().apply {
                numThreads = 4
                Log.d(TAG, "Interpreter configured with 4 threads")
            }
            Interpreter(modelBuffer, options).also {
                Log.i(TAG, "Interpreter created successfully")

                // Log output tensor info
                val outputTensor = it.getOutputTensor(0)
                Log.d(TAG, "Output tensor - Shape: ${outputTensor.shape().contentToString()}, DataType: ${outputTensor.dataType()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create interpreter", e)
            throw e
        }
    }

    private fun createImageProcessor(): ImageProcessor {
        Log.d(TAG, "Creating image processor - Input size: $inputSize, DataType: $inputDataType")

        val builder = ImageProcessor.Builder()
            .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))

        when (inputDataType) {
            DataType.UINT8 -> {
                builder.add(NormalizeOp(0f, 1f))
                Log.d(TAG, "Normalization configured for UINT8: [0, 1]")
            }

            DataType.FLOAT32 -> {
                builder.add(NormalizeOp(IMAGE_MEAN, IMAGE_STD))
                Log.d(TAG, "Normalization configured for FLOAT32: mean=$IMAGE_MEAN, std=$IMAGE_STD")
            }

            else -> {
                builder.add(NormalizeOp(0f, 255f))
                Log.d(TAG, "Normalization configured for ${inputDataType}: [0, 255]")
            }
        }

        return builder.build().also {
            Log.i(TAG, "Image processor created")
        }
    }

    fun detectGender(faceBitmap: Bitmap): GenderResult {
        Log.d(TAG, "detectGender() called - Bitmap size: ${faceBitmap.width}x${faceBitmap.height}")

        return try {
            val rgbBitmap = ensureRgbBitmap(faceBitmap)
            val tensorImage = TensorImage(inputDataType)
            tensorImage.load(rgbBitmap)
            Log.v(TAG, "Image loaded into tensor")

            val processedImage = imageProcessor.process(tensorImage)
            Log.v(TAG, "Image preprocessing completed")

            val outputTensor = interpreter.getOutputTensor(0)
            val outputShape = outputTensor.shape()
            val outputDataType = outputTensor.dataType()
            Log.v(TAG, "Output tensor info - Shape: ${outputShape.contentToString()}, DataType: $outputDataType")

            val outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType)

            synchronized(interpreter) {
                Log.d(TAG, "Running inference...")
                interpreter.run(processedImage.buffer, outputBuffer.buffer.rewind())
                Log.d(TAG, "Inference completed")
            }

            val (femaleProbability, maleProbability) = when (outputDataType) {
                DataType.FLOAT32 -> {
                    val floatArray = outputBuffer.floatArray
                    Log.v(TAG, "Raw output (FLOAT32): ${floatArray.contentToString()}")

                    if (floatArray.size >= 2) {
                        floatArray[FEMALE_INDEX] to floatArray[MALE_INDEX]
                    } else {
                        Log.e(TAG, "Unexpected output size: ${floatArray.size}, expected at least 2")
                        0.5f to 0.5f
                    }
                }

                DataType.UINT8 -> {
                    val byteArray = ByteArray(outputBuffer.buffer.remaining())
                    outputBuffer.buffer.get(byteArray)
                    Log.v(TAG, "Raw output (UINT8) size: ${byteArray.size}")

                    if (byteArray.size >= 2) {
                        val femaleProb = (byteArray[FEMALE_INDEX].toInt() and 0xFF) / 255f
                        val maleProb = (byteArray[MALE_INDEX].toInt() and 0xFF) / 255f
                        Log.v(TAG, "Converted probabilities - Female: $femaleProb, Male: $maleProb")
                        femaleProb to maleProb
                    } else {
                        Log.e(TAG, "Unexpected output size: ${byteArray.size}, expected at least 2")
                        0.5f to 0.5f
                    }
                }

                else -> {
                    Log.w(TAG, "Unsupported output data type: $outputDataType")
                    0.5f to 0.5f
                }
            }

            Log.d(TAG, "Raw probabilities - Female: $femaleProbability, Male: $maleProbability")

            // Softmax normalization
            val maxProb = maxOf(femaleProbability, maleProbability)
            val expFemale = exp(femaleProbability - maxProb)
            val expMale = exp(maleProbability - maxProb)
            val sumExp = expFemale + expMale

            val normalizedFemaleProbability = (expFemale / sumExp)
            val normalizedMaleProbability = (expMale / sumExp)

            Log.d(TAG, "Normalized probabilities - Female: $normalizedFemaleProbability, Male: $normalizedMaleProbability")

            val isFemale = normalizedFemaleProbability > normalizedMaleProbability
            val confidence =
                if (isFemale) normalizedFemaleProbability else normalizedMaleProbability

            val result = GenderResult(
                isFemale = isFemale,
                confidence = confidence
            )

            Log.i(TAG, "Gender detection result: ${if (isFemale) "Female" else "Male"} (confidence: ${String.format("%.2f", confidence * 100)}%)")

            result
        } catch (e: Exception) {
            Log.e(TAG, "Gender detection failed", e)
            GenderResult(
                isFemale = false,
                confidence = 0.5f
            )
        }
    }

    private fun ensureRgbBitmap(bitmap: Bitmap): Bitmap {
        return if (bitmap.config == Bitmap.Config.ARGB_8888) {
            Log.v(TAG, "Converting ARGB_8888 bitmap to RGB")
            val rgbBitmap = createBitmap(bitmap.width, bitmap.height)
            val canvas = Canvas(rgbBitmap)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            rgbBitmap
        } else {
            Log.v(TAG, "Bitmap already in compatible format: ${bitmap.config}")
            bitmap
        }
    }

    fun close() {
        Log.d(TAG, "Closing GenderDetectionModel")
        try {
            interpreter.close()
            Log.i(TAG, "GenderDetectionModel closed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing interpreter", e)
        }
    }

    companion object {
        private const val TAG = "GenderDetectionModel"
        private const val MODEL_FILE = "gender_class_model.tflite"
        private const val INPUT_SIZE = 224
        private const val IMAGE_MEAN = 127.5f
        private const val IMAGE_STD = 127.5f
        private const val FEMALE_INDEX = 0
        private const val MALE_INDEX = 1
    }
}

internal data class GenderResult(
    val isFemale: Boolean,
    val confidence: Float
)