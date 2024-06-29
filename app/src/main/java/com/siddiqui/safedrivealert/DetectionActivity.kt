package com.siddiqui.safedrivealert

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Surface.ROTATION_180
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.datatransport.BuildConfig
import com.google.android.gms.common.internal.safeparcel.SafeParcelableSerializer
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.siddiqui.safedrivealert.ui.main.DetectionActivityViewModel.FaceDetectionStates
import com.siddiqui.safedrivealert.ui.main.DetectionActivityViewModel.InCarStates
import com.siddiqui.safedrivealert.databinding.ActivityDetectionBinding
import com.siddiqui.safedrivealert.ui.main.DetectionActivityViewModel
import com.siddiqui.safedrivealert.ui.main.FaceDetectorProcessor


class DetectionActivity : AppCompatActivity(), FaceDetectorProcessor.OnFaceDetectListener {

    companion object {
        private const val PERMISSION_REQUESTS = 1
        private const val PENDING_INTENT_REQUESTS = 2
        private const val TRANSITIONS_RECEIVER_ACTION =
            "${BuildConfig.APPLICATION_ID}_transitions_receiver_action"


        private val REQUIRED_RUNTIME_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }


    private lateinit var binding: ActivityDetectionBinding

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var faceProcessor: FaceDetectorProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private val camSelector = CameraSelector.Builder().requireLensFacing(LENS_FACING_FRONT).build()

    private val viewModel: DetectionActivityViewModel by viewModels()

    private lateinit var safeDriveMediaPlayer: MediaPlayer
    private lateinit var unsafeDriveMediaPlayer: MediaPlayer
    private lateinit var noFaceDetectedMediaPlayer: MediaPlayer


    private val transitionBroadcastReceiver: TransitionsReceiver = TransitionsReceiver()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isRuntimePermissionsGranted()) {
            getRuntimePermissions()
        }

        safeDriveMediaPlayer = MediaPlayer.create(this, R.raw.upward)
        unsafeDriveMediaPlayer = MediaPlayer.create(this, R.raw.downward)
        noFaceDetectedMediaPlayer = MediaPlayer.create(this, R.raw.error)


        binding = ActivityDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.apply {
            processCameraProvider?.observe(this@DetectionActivity) { provider ->
                cameraProvider = provider
                bindPreviewUseCase()
            }
            uiFaceDetectionState.observe(this@DetectionActivity) { state -> updateFaceUI(state) }
            inCarDetectionState.observe(this@DetectionActivity) { state -> updateCarUI(state) }
        }

        registerActivityRecognition()
        setupLocationClient()

        binding.carMessage.setOnClickListener {
            val intent = Intent()
            intent.action = TRANSITIONS_RECEIVER_ACTION
            val result = ActivityRecognitionResult(
                DetectedActivity(
                    if (viewModel.inCarDetectionState.value != InCarStates.IN_CAR)
                        DetectedActivity.IN_VEHICLE
                    else
                        DetectedActivity.STILL,
                    100
                ), 5000, SystemClock.elapsedRealtimeNanos()
            )
            SafeParcelableSerializer.serializeToIntentExtra(
                result, intent,
                "com.google.android.location.internal.EXTRA_ACTIVITY_RESULT"
            )
            this.sendBroadcast(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        bindPreviewUseCase()
        registerReceiver(transitionBroadcastReceiver, IntentFilter(TRANSITIONS_RECEIVER_ACTION))
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        faceProcessor?.stop()
        unregisterReceiver(transitionBroadcastReceiver)
        stopLocationUpdates()
    }


    override fun onDestroy() {
        super.onDestroy()
        faceProcessor?.stop()
    }

    @SuppressLint("MissingPermission")
    private fun registerActivityRecognition() {
        val transitionReceiverPendingIntent = PendingIntent.getBroadcast(
            this, PENDING_INTENT_REQUESTS, Intent(TRANSITIONS_RECEIVER_ACTION),
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        ActivityRecognition.getClient(this)
            .requestActivityUpdates(500, transitionReceiverPendingIntent)
    }

    private fun bindPreviewUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        previewUseCase = Preview.Builder().setTargetRotation(ROTATION_180).build()
        previewUseCase!!.setSurfaceProvider(binding.previewView.surfaceProvider)
        cameraProvider!!.bindToLifecycle(this, camSelector, previewUseCase)
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun bindAnalysisUseCase() {
        unbindAnalysisUseCase() //unbind previous use-case

        val faceDetectorOptions = FaceDetectorOptions.Builder()
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setMinFaceSize(0.1f)
            .enableTracking()

        faceProcessor = FaceDetectorProcessor(faceDetectorOptions.build())
            .apply { setOnFaceDetectListener(this@DetectionActivity) }

        analysisUseCase = ImageAnalysis.Builder().build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase?.setAnalyzer(
            ContextCompat.getMainExecutor(this)
        ) { imageProxy: ImageProxy ->
            if (needUpdateGraphicOverlayImageSourceInfo) {
                binding.graphicOverlay.setImageSourceInfo(imageProxy.height, imageProxy.width, true)
                needUpdateGraphicOverlayImageSourceInfo = false
            }
            try {
                faceProcessor!!.processImageProxy(binding.graphicOverlay, imageProxy)
            } catch (e: MlKitException) {
                Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

        cameraProvider!!.bindToLifecycle(this, camSelector, analysisUseCase)
    }

    private fun unbindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }
        if (faceProcessor != null) {
            faceProcessor!!.stop()
        }
    }

    private fun isRuntimePermissionsGranted(): Boolean {
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val permissionsToRequest = ArrayList<String>()
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission.let {
                if (!isPermissionGranted(this, it)) {
                    permissionsToRequest.add(permission)
                }
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUESTS
            )
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {

        val isAndroidQOrLater: Boolean =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

        if (isAndroidQOrLater.not() && permission == Manifest.permission.ACTIVITY_RECOGNITION) {
            return true
        }

        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    override fun onDetect(results: List<Face?>) {
        binding.graphicOverlay.clear()
        if (results.isNotEmpty()) {
            val face = results[0] //Only process the first face
            val reop = face?.rightEyeOpenProbability
            val leop = face?.leftEyeOpenProbability

            if (reop == null || leop == null) {
                viewModel.updateFaceDetectionState(FaceDetectionStates.NO_FACE)
            }

            if (reop != null && leop != null) {
                if (reop < 0.1f && leop < 0.1f) {
                    viewModel.updateFaceDetectionState(FaceDetectionStates.UNSAFE)
                } else if (reop > 0.4f && leop > 0.4f) {
                    viewModel.updateFaceDetectionState(FaceDetectionStates.SAFE)
                }
            }
        } else {
            viewModel.updateFaceDetectionState(FaceDetectionStates.NO_FACE)
        }
    }


    private fun updateFaceUI(state: FaceDetectionStates) {
        safeDriveMediaPlayer.stop()
        unsafeDriveMediaPlayer.stop()
        noFaceDetectedMediaPlayer.stop()

//        if (viewModel.inCarDetectionState.value == InCarStates.OUT_CAR) return

        when (state) {
            FaceDetectionStates.SAFE -> {
                safeDriveMediaPlayer.apply { prepare();start() }
                binding.faceMessage.apply {
                    text = getString(R.string.detection_safe)
                    setBackgroundColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.transparent_holo_green_dark
                        )
                    )
                }
            }

            FaceDetectionStates.UNSAFE -> {
                unsafeDriveMediaPlayer.apply { prepare();start() }
                binding.faceMessage.apply {
                    text = getString(R.string.detection_unsafe)
                    setBackgroundColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.transparent_holo_red_dark
                        )
                    )
                }
            }

            FaceDetectionStates.NO_FACE -> {
                noFaceDetectedMediaPlayer.apply { prepare();start() }
                binding.faceMessage.apply {
                    text = getString(R.string.detection_no_face)
                    setBackgroundColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.transparent_holo_yellow_dark
                        )
                    )
                }
            }
        }
    }

    private fun updateCarUI(state: InCarStates) {
        when (state) {
            InCarStates.IN_CAR -> {
                bindAnalysisUseCase()
                binding.faceMessage.visibility = View.VISIBLE
                binding.carMessage.apply {
                    text = getString(R.string.in_vehicle)
                    setBackgroundColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.transparent_holo_green_dark
                        )
                    )
                }
            }

            InCarStates.OUT_CAR -> {
                binding.graphicOverlay.clear()
                binding.faceMessage.visibility = View.INVISIBLE
                unbindAnalysisUseCase()
                binding.carMessage.apply {
                    text = getString(R.string.out_vehicle)
                    setBackgroundColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.transparent_holo_red_dark
                        )
                    )
                }
            }
        }
    }


    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val speed = location.speed // speed in meters/second
                    val speedKmH = speed * 3.6 // convert to km/h
                    Log.d("Speed", "Speed: $speedKmH km/h")
                    Toast.makeText(
                        this@DetectionActivity,
                        "Speed: $speedKmH km/h",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.speedTextView.text =
                        getString(R.string.speed_text, speedKmH) // Update TextView
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000
        ).apply {
            setMinUpdateIntervalMillis(1000)
        }.build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    inner class TransitionsReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val res = ActivityRecognitionResult.extractResult(intent)
            viewModel.inCarDetectionState.value = InCarStates.IN_CAR

            if (res?.mostProbableActivity?.type == DetectedActivity.IN_VEHICLE) {
                viewModel.inCarDetectionState.value = InCarStates.IN_CAR
            } else {
                viewModel.inCarDetectionState.value = InCarStates.OUT_CAR
                viewModel.uiFaceDetectionState.value = FaceDetectionStates.NO_FACE
            }
        }
    }

}






//class DetectionActivity : AppCompatActivity(), FaceDetectorProcessor.OnFaceDetectListener {
//
//    companion object {
//        private const val PERMISSION_REQUESTS = 1
//
//        private val REQUIRED_RUNTIME_PERMISSIONS = arrayOf(
//            Manifest.permission.CAMERA,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        )
//    }
//
//    private lateinit var binding: ActivityDetectionBinding
//    private var cameraProvider: ProcessCameraProvider? = null
//    private var previewUseCase: Preview? = null
//    private var analysisUseCase: ImageAnalysis? = null
//    private var faceProcessor: FaceDetectorProcessor? = null
//    private var needUpdateGraphicOverlayImageSourceInfo = false
//    private val camSelector = CameraSelector.Builder().requireLensFacing(LENS_FACING_FRONT).build()
//    private val viewModel: DetectionActivityViewModel by viewModels()
//
//    private lateinit var safeDriveMediaPlayer: MediaPlayer
//    private lateinit var unsafeDriveMediaPlayer: MediaPlayer
//    private lateinit var noFaceDetectedMediaPlayer: MediaPlayer
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        if (!isRuntimePermissionsGranted()) {
//            getRuntimePermissions()
//        }
//
//        safeDriveMediaPlayer = MediaPlayer.create(this, R.raw.upward)
//        unsafeDriveMediaPlayer = MediaPlayer.create(this, R.raw.downward)
//        noFaceDetectedMediaPlayer = MediaPlayer.create(this, R.raw.error)
//
//        binding = ActivityDetectionBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        viewModel.apply {
//            processCameraProvider?.observe(this@DetectionActivity) { provider ->
//                cameraProvider = provider
//                bindPreviewUseCase()
//            }
//            uiFaceDetectionState.observe(this@DetectionActivity) { state -> updateFaceUI(state) }
//        }
//
//        setupLocationClient()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        bindAnalysisUseCase()
//        bindPreviewUseCase()
//        startLocationUpdates()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        faceProcessor?.stop()
//        stopLocationUpdates()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        faceProcessor?.stop()
//    }
//
//    private fun bindPreviewUseCase() {
//        if (cameraProvider == null) {
//            return
//        }
//        if (previewUseCase != null) {
//            cameraProvider!!.unbind(previewUseCase)
//        }
//
//        previewUseCase = Preview.Builder().setTargetRotation(ROTATION_180).build()
//        previewUseCase!!.setSurfaceProvider(binding.previewView.surfaceProvider)
//        cameraProvider!!.bindToLifecycle(this, camSelector, previewUseCase)
//    }
//
//    @SuppressLint("UnsafeOptInUsageError")
//    private fun bindAnalysisUseCase() {
//        unbindAnalysisUseCase() // Unbind previous use-case
//
//        val faceDetectorOptions = FaceDetectorOptions.Builder()
//            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
//            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
//            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
//            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
//            .setMinFaceSize(0.1f)
//            .enableTracking()
//
//        faceProcessor = FaceDetectorProcessor(faceDetectorOptions.build())
//            .apply { setOnFaceDetectListener(this@DetectionActivity) }
//
//        analysisUseCase = ImageAnalysis.Builder().build()
//
//        needUpdateGraphicOverlayImageSourceInfo = true
//
//        analysisUseCase?.setAnalyzer(
//            ContextCompat.getMainExecutor(this)
//        ) { imageProxy: ImageProxy ->
//            if (needUpdateGraphicOverlayImageSourceInfo) {
//                binding.graphicOverlay.setImageSourceInfo(imageProxy.height, imageProxy.width, true)
//                needUpdateGraphicOverlayImageSourceInfo = false
//            }
//            try {
//                faceProcessor!!.processImageProxy(binding.graphicOverlay, imageProxy)
//            } catch (e: MlKitException) {
//                Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        cameraProvider!!.bindToLifecycle(this, camSelector, analysisUseCase)
//    }
//
//    private fun unbindAnalysisUseCase() {
//        if (cameraProvider == null) {
//            return
//        }
//        if (analysisUseCase != null) {
//            cameraProvider!!.unbind(analysisUseCase)
//        }
//        if (faceProcessor != null) {
//            faceProcessor!!.stop()
//        }
//    }
//
//    private fun isRuntimePermissionsGranted(): Boolean {
//        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
//            if (!isPermissionGranted(this, permission)) {
//                return false
//            }
//        }
//        return true
//    }
//
//    private fun getRuntimePermissions() {
//        val permissionsToRequest = REQUIRED_RUNTIME_PERMISSIONS.filter {
//            !isPermissionGranted(this, it)
//        }
//        if (permissionsToRequest.isNotEmpty()) {
//            ActivityCompat.requestPermissions(
//                this,
//                permissionsToRequest.toTypedArray(),
//                PERMISSION_REQUESTS
//            )
//        }
//    }
//
//    private fun isPermissionGranted(context: Context, permission: String): Boolean {
//        return ContextCompat.checkSelfPermission(
//            context,
//            permission
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    override fun onDetect(results: List<Face?>) {
//        binding.graphicOverlay.clear()
//        if (results.isNotEmpty()) {
//            val face = results[0] // Only process the first face
//            val reop = face?.rightEyeOpenProbability
//            val leop = face?.leftEyeOpenProbability
//
//            if (reop == null || leop == null) {
//                viewModel.updateFaceDetectionState(FaceDetectionStates.NO_FACE)
//            }
//
//            if (reop != null && leop != null) {
//                if (reop < 0.1f && leop < 0.1f) {
//                    viewModel.updateFaceDetectionState(FaceDetectionStates.UNSAFE)
//                } else if (reop > 0.4f && leop > 0.4f) {
//                    viewModel.updateFaceDetectionState(FaceDetectionStates.SAFE)
//                }
//            }
//        } else {
//            viewModel.updateFaceDetectionState(FaceDetectionStates.NO_FACE)
//        }
//    }
//
//    private fun updateFaceUI(state: FaceDetectionStates) {
//        safeDriveMediaPlayer.stop()
//        unsafeDriveMediaPlayer.stop()
//        noFaceDetectedMediaPlayer.stop()
//
//        when (state) {
//            FaceDetectionStates.SAFE -> {
//                safeDriveMediaPlayer.apply { prepare(); start() }
//                binding.faceMessage.apply {
//                    text = getString(R.string.detection_safe)
//                    setBackgroundColor(
//                        ContextCompat.getColor(
//                            applicationContext,
//                            R.color.transparent_holo_green_dark
//                        )
//                    )
//                }
//            }
//            FaceDetectionStates.UNSAFE -> {
//                unsafeDriveMediaPlayer.apply { prepare(); start() }
//                binding.faceMessage.apply {
//                    text = getString(R.string.detection_unsafe)
//                    setBackgroundColor(
//                        ContextCompat.getColor(
//                            applicationContext,
//                            R.color.transparent_holo_red_dark
//                        )
//                    )
//                }
//            }
//            FaceDetectionStates.NO_FACE -> {
//                noFaceDetectedMediaPlayer.apply { prepare(); start() }
//                binding.faceMessage.apply {
//                    text = getString(R.string.detection_no_face)
//                    setBackgroundColor(
//                        ContextCompat.getColor(
//                            applicationContext,
//                            R.color.transparent_holo_yellow_dark
//                        )
//                    )
//                }
//            }
//        }
//    }
//
//    private fun setupLocationClient() {
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                for (location in locationResult.locations) {
//                    val speed = location.speed // Speed in meters/second
//                    val speedKmH = speed * 3.6 // Convert to km/h
//                    Log.d("Speed", "Speed: $speedKmH km/h")
//                    binding.speedTextView.text = getString(R.string.speed_text, speedKmH) // Update TextView
//                }
//            }
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun startLocationUpdates() {
//        val locationRequest = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            2000
//        ).apply {
//            setMinUpdateIntervalMillis(1000)
//        }.build()
//
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
//    }
//
//    private fun stopLocationUpdates() {
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//    }
//}
//
//
//
//
////
