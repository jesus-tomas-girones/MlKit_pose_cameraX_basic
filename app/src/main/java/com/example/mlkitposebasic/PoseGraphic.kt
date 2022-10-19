/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mlkitposebasic

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import java.lang.Math.max
import java.lang.Math.min
import java.util.Locale
import kotlin.math.atan2

/** Draw the detected pose in preview. */
class PoseGraphic
internal constructor(
  overlay: GraphicOverlay,
  private val pose: Pose,
  private val showInFrameLikelihood: Boolean,
  private val visualizeZ: Boolean,
  private val rescaleZForVisualization: Boolean
) : GraphicOverlay.Graphic(overlay) {
  private var zMin = java.lang.Float.MAX_VALUE
  private var zMax = java.lang.Float.MIN_VALUE
  private val classificationTextPaint: Paint = Paint()
  private val leftPaint: Paint
  private val rightPaint: Paint
  private val whitePaint: Paint

  init {
    classificationTextPaint.color = Color.WHITE
    classificationTextPaint.textSize = POSE_CLASSIFICATION_TEXT_SIZE
    classificationTextPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK)
    whitePaint = Paint()
    whitePaint.strokeWidth = STROKE_WIDTH
    whitePaint.color = Color.WHITE
    whitePaint.textSize = IN_FRAME_LIKELIHOOD_TEXT_SIZE
    leftPaint = Paint()
    leftPaint.strokeWidth = STROKE_WIDTH
    leftPaint.color = Color.GREEN
    rightPaint = Paint()
    rightPaint.strokeWidth = STROKE_WIDTH
    rightPaint.color = Color.YELLOW
  }

  override fun draw(canvas: Canvas) {
    val landmarks = pose.allPoseLandmarks
    if (landmarks.isEmpty()) {
      return
    }

    // Draw all the points
    for (landmark in landmarks) {
      drawPoint(canvas, landmark, whitePaint)
      if (visualizeZ && rescaleZForVisualization) {
        zMin = min(zMin, landmark.position3D.z)
        zMax = max(zMax, landmark.position3D.z)
      }
    }

    val nose = pose.getPoseLandmark(PoseLandmark.NOSE)
    val lefyEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER)
    val lefyEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE)
    val leftEyeOuter = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER)
    val rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER)
    val rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE)
    val rightEyeOuter = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER)
    val leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
    val rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)
    val leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH)
    val rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH)

    val leftHombro = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
    val rightHombro = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
    val leftCodo = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
    val rightCodo = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
    val leftMuñeca = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
    val rightMuñeca = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
    val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
    val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
    val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
    val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
    val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
    val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

    val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
    val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
    val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
    val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
    val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
    val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
    val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
    val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
    val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
    val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)

    // Face
    drawLine(canvas, nose, lefyEyeInner, whitePaint)
    drawLine(canvas, lefyEyeInner, lefyEye, whitePaint)
    drawLine(canvas, lefyEye, leftEyeOuter, whitePaint)
    drawLine(canvas, leftEyeOuter, leftEar, whitePaint)
    drawLine(canvas, nose, rightEyeInner, whitePaint)
    drawLine(canvas, rightEyeInner, rightEye, whitePaint)
    drawLine(canvas, rightEye, rightEyeOuter, whitePaint)
    drawLine(canvas, rightEyeOuter, rightEar, whitePaint)
    drawLine(canvas, leftMouth, rightMouth, whitePaint)

    drawLine(canvas, leftHombro, rightHombro, whitePaint)
    drawLine(canvas, leftHip, rightHip, whitePaint)

    // Left body
    drawLine(canvas, leftHombro, leftCodo, leftPaint)
    drawLine(canvas, leftCodo, leftMuñeca, leftPaint)
    drawLine(canvas, leftHombro, leftHip, leftPaint)
    drawLine(canvas, leftHip, leftKnee, leftPaint)
    drawLine(canvas, leftKnee, leftAnkle, leftPaint)
    drawLine(canvas, leftMuñeca, leftThumb, leftPaint)
    drawLine(canvas, leftMuñeca, leftPinky, leftPaint)
    drawLine(canvas, leftMuñeca, leftIndex, leftPaint)
    drawLine(canvas, leftIndex, leftPinky, leftPaint)
    drawLine(canvas, leftAnkle, leftHeel, leftPaint)
    drawLine(canvas, leftHeel, leftFootIndex, leftPaint)

    // Right body
    drawLine(canvas, rightHombro, rightCodo, rightPaint)
    drawLine(canvas, rightCodo, rightMuñeca, rightPaint)
    drawLine(canvas, rightHombro, rightHip, rightPaint)
    drawLine(canvas, rightHip, rightKnee, rightPaint)
    drawLine(canvas, rightKnee, rightAnkle, rightPaint)
    drawLine(canvas, rightMuñeca, rightThumb, rightPaint)
    drawLine(canvas, rightMuñeca, rightPinky, rightPaint)
    drawLine(canvas, rightMuñeca, rightIndex, rightPaint)
    drawLine(canvas, rightIndex, rightPinky, rightPaint)
    drawLine(canvas, rightAnkle, rightHeel, rightPaint)
    drawLine(canvas, rightHeel, rightFootIndex, rightPaint)

    // Draw inFrameLikelihood for all points
    if (showInFrameLikelihood) {
      for (landmark in landmarks) {
        canvas.drawText(
          String.format(Locale.US, "%.2f", landmark.inFrameLikelihood),
          translateX(landmark.position.x),
          translateY(landmark.position.y),
          whitePaint
        )
      }
    }
  }

  fun getAngle(firstPoint: PoseLandmark?, midPoint: PoseLandmark?, lastPoint:PoseLandmark?):Double {
    var result = Math.toDegrees(
      (  atan2(lastPoint!!.position.x - midPoint!!.position.x,
               lastPoint!!.position.y - midPoint!!.position.y)
       - atan2(firstPoint!!.position.x - midPoint!!.position.x,
               firstPoint!!.position.y - midPoint!!.position.y)).toDouble() )
    result = Math.abs(result)// Angle should never be negative
    if (result > 180) {
      result = 360.0 - result// Always get the acute representation of the angle
    }
    return result
  }

  internal fun drawPoint(canvas: Canvas, landmark: PoseLandmark, paint: Paint) {
    val point = landmark.position3D
    updatePaintColorByZValue(
      paint,
      canvas,
      visualizeZ,
      rescaleZForVisualization,
      point.z,
      zMin,
      zMax
    )
    canvas.drawCircle(translateX(point.x), translateY(point.y), DOT_RADIUS, paint)
  }

  internal fun drawLine(
    canvas: Canvas,
    startLandmark: PoseLandmark?,
    endLandmark: PoseLandmark?,
    paint: Paint
  ) {
    val start = startLandmark!!.position3D
    val end = endLandmark!!.position3D

    // Gets average z for the current body line
    val avgZInImagePixel = (start.z + end.z) / 2
    updatePaintColorByZValue(
      paint,
      canvas,
      visualizeZ,
      rescaleZForVisualization,
      avgZInImagePixel,
      zMin,
      zMax
    )

    canvas.drawLine(
      translateX(start.x),
      translateY(start.y),
      translateX(end.x),
      translateY(end.y),
      paint
    )
  }

  companion object {
    private val DOT_RADIUS = 8.0f
    private val IN_FRAME_LIKELIHOOD_TEXT_SIZE = 30.0f
    private val STROKE_WIDTH = 10.0f
    private val POSE_CLASSIFICATION_TEXT_SIZE = 60.0f
  }
}
