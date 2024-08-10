package com.siddiqui.safedrivealert.ui.main

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.TaskExecutors
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*



import java.util.*


class FaceDetectorProcessor(detectorOptions: FaceDetectorOptions?) {

    interface OnFaceDetectListener {
        fun onDetect(results: List<Face?>)
    }

    private val detector: FaceDetector
    private var onFaceDetectListener: OnFaceDetectListener? = null

    private val fpsTimer = Timer()
    private val executor = ScopedExecutor(TaskExecutors.MAIN_THREAD)

    private var isShutdown = false

    // Frame count that have been processed so far in an one second interval to calculate FPS.
    private var frameProcessedInInterval = 0

    companion object {
//        const val MANUAL_TESTING_LOG = "LogTagForTest"
//        private const val TAG = "FaceDetectorProcessor"

        private fun logExtrasForTesting(face: Face?) {
            if (face != null) {

                val landMarkTypes = intArrayOf(
                    FaceLandmark.MOUTH_BOTTOM, FaceLandmark.MOUTH_RIGHT,
                    FaceLandmark.MOUTH_LEFT, FaceLandmark.RIGHT_EYE,
                    FaceLandmark.LEFT_EYE, FaceLandmark.RIGHT_EAR,
                    FaceLandmark.LEFT_EAR, FaceLandmark.RIGHT_CHEEK,
                    FaceLandmark.LEFT_CHEEK, FaceLandmark.NOSE_BASE
                )

                for (i in landMarkTypes.indices) {
                    val landmark = face.getLandmark(landMarkTypes[i])
                    if (landmark == null) {
//                        Log.v(
//                            MANUAL_TESTING_LOG,
//                            "No landmark of type: " + landMarkTypesStrings[i] + " has been detected"
//                        )
                    } else {
                        val landmarkPosition = landmark.position
                        val landmarkPositionStr =
                            String.format(
                                Locale.US, "x: %f , y: %f", landmarkPosition.x, landmarkPosition.y
                            )
//                        Log.v(
//                            MANUAL_TESTING_LOG,
//                            "Position for face landmark: " +
//                                    landMarkTypesStrings[i] +
//                                    " is :" +
//                                    landmarkPositionStr
//                        )
                    }
                }
            }
        }
    }

    init {

        // a timer, for processing images every five seconds
        fpsTimer.schedule(
            object : TimerTask() {
                override fun run() {
                    frameProcessedInInterval = 0
                }
            }, 0, 1000
        )

        val options = detectorOptions
            ?: FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .enableTracking()
                .build()

        detector = FaceDetection.getClient(options)
}

    @ExperimentalGetImage
    fun processImageProxy(graphicOverlay: GraphicOverlay, image: ImageProxy) {
        if (isShutdown) return

        detector.process(InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees))

            .addOnCompleteListener { image.close() }
            .addOnSuccessListener(executor) { results ->
                graphicOverlay.clear()
                if (results.isNotEmpty()){
                    graphicOverlay.add(FaceGraphic(graphicOverlay, results[0], graphicOverlay.context))
                    graphicOverlay.postInvalidate()
                }

                if (frameProcessedInInterval > 0) return@addOnSuccessListener
                frameProcessedInInterval++

                if (results == null) return@addOnSuccessListener

                onFaceDetectListener?.onDetect(results)
                for (face in results) {
                    logExtrasForTesting(face)
                }
            }
            .addOnFailureListener(executor) { e ->
                e.printStackTrace()
            }

    }

    fun stop() {
        executor.shutdown()
        isShutdown = true
        fpsTimer.cancel()
        detector.close()
    }

    fun setOnFaceDetectListener(l: OnFaceDetectListener) {
        onFaceDetectListener = l
    }
}
