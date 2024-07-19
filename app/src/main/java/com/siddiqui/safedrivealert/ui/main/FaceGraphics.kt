package com.siddiqui.safedrivealert.ui.main

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.face.Face

class FaceGraphic constructor(overlay: GraphicOverlay?, private val face: Face) :
    GraphicOverlay.Graphic(overlay) {
    private val facePositionPaint: Paint
    private val rectBoxPaint = Paint().apply {
        color = Color.TRANSPARENT
        style = Paint.Style.STROKE
        strokeWidth = BOX_STROKE_WIDTH
    }

    init {
        val selectedColor = Color.TRANSPARENT
        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor
    }
    override fun draw(canvas: Canvas) {
        val x = translateX(face.boundingBox.centerX().toFloat())
        val y = translateY(face.boundingBox.centerY().toFloat())
        val left = x - scale(face.boundingBox.width() / 2.0f)
        val top = y - scale(face.boundingBox.height() / 2.0f)
        val right = x + scale(face.boundingBox.width() / 2.0f)
        val bottom = y + scale(face.boundingBox.height() / 2.0f)
        val lineHeight = ID_TEXT_SIZE + BOX_STROKE_WIDTH
        var yLabelOffset: Float = if (face.trackingId == null) 0f else -lineHeight

        yLabelOffset += ID_TEXT_SIZE
        canvas.drawRect(left, top, right, bottom, rectBoxPaint)
    }

    companion object {
        private const val ID_TEXT_SIZE = 30.0f
        private const val BOX_STROKE_WIDTH = 5.0f
    }
}