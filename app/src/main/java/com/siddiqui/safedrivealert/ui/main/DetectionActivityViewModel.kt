package com.siddiqui.safedrivealert.ui.main

import android.app.Application
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException

class DetectionActivityViewModel(application: Application) : AndroidViewModel(application) {

    enum class FaceDetectionStates(val intervalTolerancePolicy: Int) {
        SAFE(1),
        UNSAFE(0),
        NO_FACE(0)
    }

    enum class InCarStates { IN_CAR, OUT_CAR }

    private var cameraProviderLiveData: MutableLiveData<ProcessCameraProvider>? = null

    val faceDetectionState = MutableLiveData(FaceDetectionStates.NO_FACE)
    val uiFaceDetectionState = MutableLiveData(FaceDetectionStates.NO_FACE)

    private var stateChangeCounter = 0

    val inCarDetectionState = MutableLiveData(InCarStates.OUT_CAR)
    val processCameraProvider: MutableLiveData<ProcessCameraProvider>?
        get() {
            if (cameraProviderLiveData == null) {
                cameraProviderLiveData = MutableLiveData<ProcessCameraProvider>()
                val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                    ProcessCameraProvider.getInstance(getApplication())
                cameraProviderFuture.addListener(
                    {
                        try {
                            cameraProviderLiveData!!.setValue(cameraProviderFuture.get())
                        } catch (e: ExecutionException) {
                            Log.e("CameraX", "Unhandled exception", e)
                        }
                    },
                    ContextCompat.getMainExecutor(getApplication())
                )
            }
            return cameraProviderLiveData
        }

    fun updateFaceDetectionState(state:FaceDetectionStates){
        faceDetectionState.value = state

        if (faceDetectionState.value == uiFaceDetectionState.value) {
            stateChangeCounter = 0
            return
        }

        if (stateChangeCounter < uiFaceDetectionState.value?.intervalTolerancePolicy!!) {
            stateChangeCounter++
            return
        }

        stateChangeCounter = 0
        uiFaceDetectionState.value= faceDetectionState.value
    }

}