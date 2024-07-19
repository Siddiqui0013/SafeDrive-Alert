package com.siddiqui.safedrivealert
//
//import android.os.Bundle
//import android.util.Log
//import android.view.SurfaceView
//import android.view.View
//import android.view.WindowManager
//import android.widget.Toast
//import org.opencv.android.CameraActivity
//import org.opencv.android.CameraBridgeViewBase
//import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
//import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
//import org.opencv.android.OpenCVLoader
//import org.opencv.core.Mat
//import org.opencv.core.Point
//import org.opencv.core.Scalar
//
//import org.opencv.core.Core
//import org.opencv.core.CvType
//import org.opencv.core.MatOfPoint
//
//
//import org.opencv.imgproc.Imgproc
//import kotlin.math.abs
//
//class LaneActivity : CameraActivity(), CvCameraViewListener2 {
//
//    private var mOpenCvCameraView: CameraBridgeViewBase? = null
//
//
//    public override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        if (OpenCVLoader.initLocal()) {
//            Toast.makeText(this, "OpenCV initialization Success!", Toast.LENGTH_LONG).show()
//        } else {
//            Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG).show()
//            return
//        }
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        setContentView(R.layout.activity_lane)
//        mOpenCvCameraView =
//            findViewById<View>(R.id.camera_view) as CameraBridgeViewBase
//        mOpenCvCameraView!!.setVisibility(SurfaceView.VISIBLE)
//        mOpenCvCameraView!!.setCvCameraViewListener(this)
//    }
//
//    public override fun onPause() {
//        super.onPause()
//        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
//    }
//
//    public override fun onResume() {
//        super.onResume()
//        if (mOpenCvCameraView != null) mOpenCvCameraView!!.enableView()
//    }
//
//    override fun getCameraViewList(): List<CameraBridgeViewBase> {
//        return mOpenCvCameraView?.let { listOf(it) } ?: emptyList()
//    }
//
//    public override fun onDestroy() {
//        super.onDestroy()
//        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
//    }
//
//    override fun onCameraViewStarted(width: Int, height: Int) {}
//    override fun onCameraViewStopped() {}
//    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
//        val frame = inputFrame.rgba()
//        detectLanes(frame)
////        convertToGrayscale(frame)
//        return frame
//    }
//
////    private fun convertToGrayscale(mat: Mat) {
////        val gray = Mat()
////        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY)
////        mat.release()
////        mat.setTo(gray)
////        gray.release()
////    }
////}
//
//
////    private fun detectLanes(mat: Mat) {
////        val gray = Mat()
////        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY)
////        Imgproc.GaussianBlur(gray, gray, org.opencv.core.Size(5.0, 5.0), 0.0)
////        val edges = Mat()
////        Imgproc.Canny(gray, edges, 50.0, 150.0)
////
////        val lines = Mat()
////        Imgproc.HoughLinesP(edges, lines, 1.0, Math.PI / 180, 50, 50.0, 10.0)
////
////        for (i in 0 until lines.rows()) {
////            val l = lines.row(i)
////            val points = IntArray(4)
////            l.get(0, 0, points)
////            val p1 = Point(points[0].toDouble(), points[1].toDouble())
////            val p2 = Point(points[2].toDouble(), points[3].toDouble())
////            Imgproc.line(mat, p1, p2, Scalar(0.0, 255.0, 0.0), 3)
////        }
////
////        gray.release()
////        edges.release()
////        lines.release()
////    }
////}
//
//
//    private fun detectLanes(mat: Mat) {
//        // Convert to grayscale
//        val gray = Mat()
//        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY)
//
//        // Gaussian blur to reduce noise and improve edge detection
//        Imgproc.GaussianBlur(gray, gray, org.opencv.core.Size(5.0, 5.0), 0.0)
//
//        // Canny edge detection
//        val edges = Mat()
//        Imgproc.Canny(gray, edges, 50.0, 150.0)
//
//        // Define region of interest (ROI)
//        val mask = Mat.zeros(edges.size(), CvType.CV_8UC1)
//        val roi = MatOfPoint(
//            Point((edges.cols() * 0.1).toInt().toDouble(), edges.rows().toDouble()),
//            Point((edges.cols() * 0.45).toInt().toDouble(), (edges.rows() * 0.6).toInt().toDouble()),
//            Point((edges.cols() * 0.55).toInt().toDouble(), (edges.rows() * 0.6).toInt().toDouble()),
//            Point((edges.cols() * 0.9).toInt().toDouble(), edges.rows().toDouble())
//        )
//        Imgproc.fillPoly(mask, listOf(roi), Scalar(255.0))
//
//        // Mask the edges image to only show region of interest
//        val maskedEdges = Mat()
//        Core.bitwise_and(edges, mask, maskedEdges)
//
//        // Hough line transform
//        val lines = Mat()
//        Imgproc.HoughLinesP(maskedEdges, lines, 1.0, Math.PI / 180, 50, 50.0, 10.0)
//
//        // Filter and draw lines
//        for (i in 0 until lines.rows()) {
//            val l = lines.row(i)
//            val points = IntArray(4)
//            l.get(0, 0, points)
//            val p1 = Point(points[0].toDouble(), points[1].toDouble())
//            val p2 = Point(points[2].toDouble(), points[3].toDouble())
//            val slope = (p2.y - p1.y) / (p2.x - p1.x)
//
//            // Filter lines based on slope
//            if (abs(slope) > 0.5) { // Adjust the slope threshold as needed
//                Imgproc.line(mat, p1, p2, Scalar(0.0, 255.0, 0.0), 3)
//            }
//        }
//
//        // Release resources
//        gray.release()
//        edges.release()
//        mask.release()
//        maskedEdges.release()
//        lines.release()
//    }
//}

import android.os.Bundle
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
import org.opencv.core.CvType
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc
import org.opencv.core.Core
import kotlin.math.sqrt
import kotlin.concurrent.thread

class LaneActivity : CameraActivity(), CvCameraViewListener2 {

    private var mOpenCvCameraView: CameraBridgeViewBase? = null

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
        mOpenCvCameraView = findViewById<View>(R.id.camera_view) as CameraBridgeViewBase
        mOpenCvCameraView!!.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView!!.setCvCameraViewListener(this)
    }

    public override fun onPause() {
        super.onPause()
        mOpenCvCameraView?.disableView()
    }

    public override fun onResume() {
        super.onResume()
        mOpenCvCameraView?.enableView()
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase> {
        return mOpenCvCameraView?.let { listOf(it) } ?: emptyList()
    }

    public override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView?.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}
    override fun onCameraViewStopped() {}
    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        val frame = inputFrame.rgba()
        return processFrame(frame)
    }

    private fun processFrame(frame: Mat): Mat {
        val gray = Mat()
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY)

        val blur = Mat()
        Imgproc.GaussianBlur(gray, blur, org.opencv.core.Size(5.0, 5.0), 0.0)

        val edges = Mat()
        Imgproc.Canny(blur, edges, 50.0, 150.0)

        val region = regionSelection(edges)

        val lines = houghTransform(region)

        val result = drawLaneLines(frame, laneLines(frame, lines))

        gray.release()
        blur.release()
        edges.release()
        region.release()
        lines.release()

        return result
    }

    private fun regionSelection(image: Mat): Mat {
        val mask = Mat.zeros(image.size(), CvType.CV_8UC1)
        val vertices = MatOfPoint(
            Point(image.cols() * 0.1, image.rows() * 0.95),
            Point(image.cols() * 0.4, image.rows() * 0.6),
            Point(image.cols() * 0.6, image.rows() * 0.6),
            Point(image.cols() * 0.9, image.rows() * 0.95)
        )
        val points = listOf(vertices)
        Imgproc.fillPoly(mask, points, Scalar(255.0))
        val maskedImage = Mat()
        Core.bitwise_and(image, mask, maskedImage)
        mask.release()
        return maskedImage
    }

    private fun houghTransform(image: Mat): Mat {
        val lines = Mat()
        Imgproc.HoughLinesP(
            image, lines, 1.0, Math.PI / 180, 20,
            20.0, 50.0
        )
        return lines
    }

    private fun averageSlopeIntercept(lines: Mat): Pair<DoubleArray?, DoubleArray?> {
        val leftLines = mutableListOf<Pair<Double, Double>>()
        val leftWeights = mutableListOf<Double>()
        val rightLines = mutableListOf<Pair<Double, Double>>()
        val rightWeights = mutableListOf<Double>()

        for (i in 0 until lines.rows()) {
            val line = lines.get(i, 0)
            val x1 = line[0]
            val y1 = line[1]
            val x2 = line[2]
            val y2 = line[3]
            if (x1 == x2) continue
            val slope = (y2 - y1) / (x2 - x1)
            val intercept = y1 - (slope * x1)
            val length = sqrt(((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1)))
            if (slope < 0) {
                leftLines.add(Pair(slope, intercept))
                leftWeights.add(length)
            } else {
                rightLines.add(Pair(slope, intercept))
                rightWeights.add(length)
            }
        }

        val leftLane = if (leftWeights.isNotEmpty()) {
            val leftSlopeIntercept = DoubleArray(2)
            leftSlopeIntercept[0] = leftLines.zip(leftWeights)
                .sumOf { it.first.first * it.second } / leftWeights.sum()
            leftSlopeIntercept[1] = leftLines.zip(leftWeights)
                .sumOf { it.first.second * it.second } / leftWeights.sum()
            leftSlopeIntercept
        } else {
            null
        }

        val rightLane = if (rightWeights.isNotEmpty()) {
            val rightSlopeIntercept = DoubleArray(2)
            rightSlopeIntercept[0] = rightLines.zip(rightWeights)
                .sumOf { it.first.first * it.second } / rightWeights.sum()
            rightSlopeIntercept[1] = rightLines.zip(rightWeights)
                .sumOf { it.first.second * it.second } / rightWeights.sum()
            rightSlopeIntercept
        } else {
            null
        }

        return Pair(leftLane, rightLane)
    }

    private fun pixelPoints(y1: Double, y2: Double, line: DoubleArray?): Pair<Point, Point>? {
        if (line == null) return null
        val slope = line[0]
        val intercept = line[1]
        val x1 = (y1 - intercept) / slope
        val x2 = (y2 - intercept) / slope
        return Pair(Point(x1, y1), Point(x2, y2))
    }

    private fun laneLines(image: Mat, lines: Mat): List<Pair<Point, Point>?> {
        val (leftLane, rightLane) = averageSlopeIntercept(lines)
        val y1 = image.rows().toDouble()
        val y2 = y1 * 0.6
        val leftLine = pixelPoints(y1, y2, leftLane)
        val rightLine = pixelPoints(y1, y2, rightLane)
        return listOf(leftLine, rightLine)
    }

    private fun drawLaneLines(image: Mat, lines: List<Pair<Point, Point>?>, color: Scalar = Scalar(255.0, 0.0, 0.0), thickness: Int = 12): Mat {
        val lineImage = Mat.zeros(image.size(), image.type())
        for (line in lines) {
            if (line != null) {
                Imgproc.line(lineImage, line.first, line.second, color, thickness)
            }
        }
        val result = Mat()
        Core.addWeighted(image, 1.0, lineImage, 1.0, 0.0, result)
        lineImage.release()
        return result
    }
}


