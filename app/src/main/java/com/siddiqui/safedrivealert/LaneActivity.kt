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
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class LaneActivity : CameraActivity(), CvCameraViewListener2 {

    private var mOpenCvCameraView: CameraBridgeViewBase? = null


    init {
        Log.i("TAG", "Instantiated new " + this.javaClass)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (OpenCVLoader.initLocal()) {
            Toast.makeText(this, "OpenCV initialization Success!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG).show()
            return
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_lane)
        mOpenCvCameraView =
            findViewById<View>(R.id.camera_view) as CameraBridgeViewBase
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
        detectLanes(frame)
        return frame
    }

    private fun detectLanes(mat: Mat) {
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.GaussianBlur(gray, gray, org.opencv.core.Size(5.0, 5.0), 0.0)
        val edges = Mat()
        Imgproc.Canny(gray, edges, 50.0, 150.0)

        val lines = Mat()
        Imgproc.HoughLinesP(edges, lines, 1.0, Math.PI / 180, 50, 50.0, 10.0)

        for (i in 0 until lines.rows()) {
            val l = lines.row(i)
            val points = IntArray(4)
            l.get(0, 0, points)
            val p1 = Point(points[0].toDouble(), points[1].toDouble())
            val p2 = Point(points[2].toDouble(), points[3].toDouble())
            Imgproc.line(mat, p1, p2, Scalar(0.0, 255.0, 0.0), 3)
        }

        gray.release()
        edges.release()
        lines.release()
    }

}