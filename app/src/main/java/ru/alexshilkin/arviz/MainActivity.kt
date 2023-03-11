package ru.alexshilkin.arviz

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCamera2View
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.security.Permissions

class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private val cameraView by lazy { findViewById<JavaCamera2View>(R.id.cameraView) }
    lateinit var imageMat: Mat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setContentView(R.layout.activity_main)

        if (allPermissionsGranted()){
            Log.d(TAG, "All permission granted")
            initOpenCvAndCamera()
        }else {
            Log.d(TAG, "Request permission")
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.d(TAG, "onCameraViewStarted width = $width, height = $height")
        imageMat = Mat(width, height, CvType.CV_8UC4)
    }

    override fun onCameraViewStopped() {
        imageMat.release()
        Log.d(TAG, "onCameraViewStopped")
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        Log.d(TAG, "onCameraFrame")
        imageMat = inputFrame.rgba()
        return imageMat
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS){
            if (allPermissionsGranted()){
                initOpenCvAndCamera()
                Log.d(TAG, "Permission granted")
            }else {
                Log.d(TAG, "Permission denied")
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        cameraView?.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraView?.disableView()
    }

    private fun initOpenCvAndCamera(){
        cameraView.visibility = SurfaceView.VISIBLE
        cameraView.setCameraPermissionGranted()
        cameraView.setCameraIndex(
            CameraCharacteristics.LENS_FACING_FRONT)
        cameraView.setCvCameraViewListener(this@MainActivity)

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV load success")
            cameraView.enableView()
            cameraView.enableFpsMeter()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "MainActivity"

        private const val REQUEST_CODE_PERMISSIONS = 111
        private val REQUIRED_PERMISSIONS = arrayOf(
            CAMERA,
            WRITE_EXTERNAL_STORAGE,
            READ_EXTERNAL_STORAGE,
            RECORD_AUDIO,
            ACCESS_FINE_LOCATION
        )
    }
}