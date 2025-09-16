package com.ae.imageharamblur.faceDetection

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


internal class FaceDetector() {

    private val mlKitDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setMinFaceSize(0.05f)
            .build()
    )

    suspend fun detectFaces(bitmap: Bitmap): List<DetectedFace> = withContext(Dispatchers.Default) {
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val faces = mlKitDetector.process(inputImage).await()

            faces.map { face ->
                DetectedFace(boundingBox = face.boundingBox)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun close() {
        mlKitDetector.close()
    }
}