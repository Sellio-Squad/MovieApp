package com.ae.imageharamblur.faceDetection

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "FaceDetector"

internal class FaceDetector() {

    private val mlKitDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setMinFaceSize(0.05f) // 5% of image - detects very small/distant faces
            .build()
    )

    suspend fun detectFaces(bitmap: Bitmap): List<DetectedFace> = withContext(Dispatchers.Default) {
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val faces = mlKitDetector.process(inputImage).await()

            Log.d(TAG, "Detected ${faces.size} faces")

            faces.map { face ->
                DetectedFace(boundingBox = face.boundingBox)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting faces", e)
            emptyList()
        }
    }

    fun close() {
        mlKitDetector.close()
    }
}