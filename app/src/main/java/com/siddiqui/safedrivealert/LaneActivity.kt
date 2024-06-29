package com.siddiqui.safedrivealert

import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.Core
import org.opencv.core.Size
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio


class LaneActivity : CameraActivity(), CvCameraViewListener2 {

    private var mOpenCvCameraView: CameraBridgeViewBase? = null

    val image = Mat()
//    val input = VideoCapture(inputFilePath)
    val size = Size(input.get(Videoio.CAP_PROP_FRAME_WIDTH),input.get(Videoio.CAP_PROP_FRAME_HEIGHT))


    init {
        Log.i(TAG, "Instantiated new " + this.javaClass)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "called onCreate")
        super.onCreate(savedInstanceState)
        if (OpenCVLoader.initLocal()) {
            Toast.makeText(this, "OpenCV initialization Success!", Toast.LENGTH_LONG).show()
            Log.i(TAG, "OpenCV loaded successfully")
        } else {
            Log.e(TAG, "OpenCV initialization failed!")
            Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG).show()
            return
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_lane)
        mOpenCvCameraView =
            findViewById<View>(R.id.tutorial1_activity_java_surface_view) as CameraBridgeViewBase
        mOpenCvCameraView!!.setVisibility(SurfaceView.VISIBLE)
        mOpenCvCameraView!!.setCvCameraViewListener(this)
    }

    public override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
    }

    public override fun onResume() {
        super.onResume()
        if (mOpenCvCameraView != null) mOpenCvCameraView!!.enableView()
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase> {
        return mOpenCvCameraView?.let { listOf(it) } ?: emptyList()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}
    override fun onCameraViewStopped() {}
    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()
//        val rotatedFrame = Mat()
//
//        Core.transpose(frame, rotatedFrame)
//        Core.flip(rotatedFrame, rotatedFrame, 1)
//        return rotatedFrame

        return frame

    }

    companion object {
        private const val TAG = "OCVSample::Activity"
    }
}