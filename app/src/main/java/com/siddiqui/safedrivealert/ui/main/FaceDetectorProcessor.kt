package com.siddiqui.safedrivealert.ui.main

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

    private var frameProcessedInInterval = 0


    init {

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
