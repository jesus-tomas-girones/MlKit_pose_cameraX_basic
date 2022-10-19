package com.example.mlkitposebasic

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mlkitposebasic.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias PoseResultListener = (seDetectaCuerpo : Boolean) -> Unit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var poseDetector : PoseDetector
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var graphicOverlay: GraphicOverlay

    private class PoseAnalyzer(val poseDetector: PoseDetector,
                               val graphicOverlay: GraphicOverlay,
                               val cameraSelector: CameraSelector,
                               val listener: PoseResultListener) : ImageAnalysis.Analyzer {
        @SuppressLint("UnsafeOptInUsageError", "RestrictedApi")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                //TODO Posiblemente este código pueda ejecutarse solo una vez cuando se selecciona la cámara
                val isImageFlipped = cameraSelector.lensFacing == CameraSelector.LENS_FACING_FRONT
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
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
                            val poseGraphic = PoseGraphic(graphicOverlay,pose, false,false,false)
                            graphicOverlay.add(poseGraphic)
                        }
                        imageProxy.close()
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        listener(false)
                        imageProxy.close()
                    }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        graphicOverlay = binding.graphicOverlay

        // Accurate pose detector
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()
        poseDetector = PoseDetection.getClient(options)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            //Analyzer
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA //.DEFAULT_FRONT_CAMERA
            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, PoseAnalyzer (poseDetector, graphicOverlay, cameraSelector){ hayCuerpo ->
                        Log.d(TAG, "Se detecta cuerpo: $hayCuerpo")
                    })
                }
            try {
                cameraProvider.unbindAll()  // Unbind use cases before rebinding
                cameraProvider.bindToLifecycle( // Bind use cases to camera
                    this, cameraSelector, preview,  imageAnalyzer)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "PoseDetectBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}