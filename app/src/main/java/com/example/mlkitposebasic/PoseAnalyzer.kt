package com.example.mlkitposebasic

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions

typealias PoseResultListener = (seDetectaPersona : Boolean) -> Unit //Información que devolverá el Listener

/**
 * Analiza una imagen para detectar con PoseDetection (de ML Kit) si hay yna pose.
 * En tal caso visualiza la pose en una vista de tipo GraphicOverlay y avisa a un listener.
 *
 * @param graphicOverlay vista donde se mostrará la pose detectada
 * @param isImageFlipped Cuando la cámara es delantera, la imagen está volteada verticalmente
 * @param listener método al que avisaremos tras cada reconocimiento, indicando si se detecta persona
 *
 * @property poseDetector Objeto de ML Kit que hace la detección de poses
 * @property options define propiedades de {@link poseDetector} en nuestro caso, modo preciso y en streaming
 */
class PoseAnalyzer(private val graphicOverlay: GraphicOverlay,
                   private val isImageFlipped: Boolean,
                   val listener: PoseResultListener) : ImageAnalysis.Analyzer {
    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
        .build()
    private val poseDetector = PoseDetection.getClient(options)

    /**
     * Método que será llamado cada vez que se disponga de una nueva imagen
     *
     * @param imageProxy imagen a procesar. La clase ImageProxy contiene información sobre si la imagen capturada tenía una rotación
     *
     *
     */
    @SuppressLint("UnsafeOptInUsageError", "RestrictedApi")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees //Rotación de la captura
            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees) //Obtenemos image corregida

            //Calcumamos el ajuste entre el tamaño de visualización (graphicOverlay) y la imagen capturada (imageProxy)
            //TODO Posiblemente este código pueda ejecutarse solo una vez cuando se selecciona la cámara
            if (rotationDegrees == 0 || rotationDegrees == 180) {
                graphicOverlay.setImageSourceInfo(imageProxy.width, imageProxy.height, isImageFlipped)
            } else {
                graphicOverlay.setImageSourceInfo(imageProxy.height, imageProxy.width, isImageFlipped)
            }
            // Pass image to an ML Kit Vision API
            poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    // Task completed successfully
                    graphicOverlay.clear()
                    if (pose.allPoseLandmarks.isEmpty())
                        listener(false)
                    else {
                        listener(true)
                        val poseGraphic = PoseGraphic(graphicOverlay,pose,
                            showInFrameLikelihood = true,
                            visualizeZ = true,
                            rescaleZForVisualiz = true
                        )
                        graphicOverlay.add(poseGraphic)
                    }
                    imageProxy.close()
                }
                .addOnFailureListener { // Task failed with an exception
                    listener(false)
                    imageProxy.close()
                }
        }
    }
}
