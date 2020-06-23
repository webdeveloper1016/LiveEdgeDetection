package info.hannes.liveedgedetection.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import info.hannes.liveedgedetection.*
import info.hannes.liveedgedetection.view.ScanSurfaceView
import kotlinx.android.synthetic.main.activity_scan.*
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import timber.log.Timber
import java.util.*

/**
 * This class initiates camera and detects edges on live view
 */
class ScanActivity : AppCompatActivity(), IScanner, View.OnClickListener {
    private var imageSurfaceView: ScanSurfaceView? = null
    private var isPermissionNotGranted = false
    private var copyBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        init()
    }

    private fun init() {
        crop_accept_btn.setOnClickListener(this)
        crop_reject_btn.setOnClickListener {
            TransitionManager.beginDelayedTransition(container_scan)
            crop_layout.visibility = View.GONE
            imageSurfaceView?.setPreviewCallback()
        }
        checkCameraPermissions()
    }

    private fun checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            isPermissionNotGranted = true
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Enable camera permission from settings", Toast.LENGTH_SHORT).show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                        MY_PERMISSIONS_REQUEST_CAMERA)
            }
        } else {
            if (!isPermissionNotGranted) {
                imageSurfaceView = ScanSurfaceView(this@ScanActivity, this)
                camera_preview.addView(imageSurfaceView)
            } else {
                isPermissionNotGranted = false
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> onRequestCamera(grantResults)
            else -> {
            }
        }
    }

    private fun onRequestCamera(grantResults: IntArray) {
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Handler().postDelayed({
                runOnUiThread {
                    imageSurfaceView = ScanSurfaceView(this@ScanActivity, this@ScanActivity)
                    camera_preview.addView(imageSurfaceView)
                }
            }, 500)
        } else {
            Toast.makeText(this, getString(R.string.camera_activity_permission_denied_toast), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun displayHint(scanHint: ScanHint) {
        capture_hint_layout.visibility = View.VISIBLE
        when (scanHint) {
            ScanHint.MOVE_CLOSER -> {
                capture_hint_text.text = resources.getString(R.string.move_closer)
                capture_hint_layout.background = resources.getDrawable(R.drawable.hint_red)
            }
            ScanHint.MOVE_AWAY -> {
                capture_hint_text.text = resources.getString(R.string.move_away)
                capture_hint_layout.background = resources.getDrawable(R.drawable.hint_red)
            }
            ScanHint.ADJUST_ANGLE -> {
                capture_hint_text.text = resources.getString(R.string.adjust_angle)
                capture_hint_layout.background = resources.getDrawable(R.drawable.hint_red)
            }
            ScanHint.FIND_RECT -> {
                capture_hint_text.text = resources.getString(R.string.finding_rect)
                capture_hint_layout.background = resources.getDrawable(R.drawable.hint_white)
            }
            ScanHint.CAPTURING_IMAGE -> {
                capture_hint_text.text = resources.getString(R.string.hold_still)
                capture_hint_layout.background = resources.getDrawable(R.drawable.hint_green)
            }
            ScanHint.NO_MESSAGE -> capture_hint_layout.visibility = View.GONE
        }
    }

    override fun onPictureClicked(bitmap: Bitmap) {
        try {
            copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val height = window.findViewById<View>(Window.ID_ANDROID_CONTENT).height
            val width = window.findViewById<View>(Window.ID_ANDROID_CONTENT).width
            copyBitmap = ScanUtils.resizeToScreenContentSize(copyBitmap, width, height)
            copyBitmap?.let {
                val originalMat = Mat(it.getHeight(), it.getWidth(), CvType.CV_8UC1)
                Utils.bitmapToMat(copyBitmap, originalMat)
                val points: ArrayList<PointF>
                val pointFs: MutableMap<Int, PointF> = HashMap()
                try {
                    val quad = ScanUtils.detectLargestQuadrilateral(originalMat)
                    if (null != quad) {
                        val resultArea = Math.abs(Imgproc.contourArea(quad.contour))
                        val previewArea = originalMat.rows() * originalMat.cols().toDouble()
                        if (resultArea > previewArea * 0.08) {
                            points = ArrayList()
                            points.add(PointF(quad.points[0].x.toFloat(), quad.points[0].y.toFloat()))
                            points.add(PointF(quad.points[1].x.toFloat(), quad.points[1].y.toFloat()))
                            points.add(PointF(quad.points[3].x.toFloat(), quad.points[3].y.toFloat()))
                            points.add(PointF(quad.points[2].x.toFloat(), quad.points[2].y.toFloat()))
                        } else {
                            points = ScanUtils.getPolygonDefaultPoints(copyBitmap)
                        }
                    } else {
                        points = ScanUtils.getPolygonDefaultPoints(copyBitmap)
                    }
                    var index = -1
                    for (pointF in points) {
                        pointFs[++index] = pointF
                    }
                    polygon_view.points = pointFs
                    val padding = resources.getDimension(R.dimen.scan_padding).toInt()
                    val layoutParams = FrameLayout.LayoutParams(it.getWidth() + 2 * padding, it.getHeight() + 2 * padding)
                    layoutParams.gravity = Gravity.CENTER
                    polygon_view.layoutParams = layoutParams
                    TransitionManager.beginDelayedTransition(container_scan)
                    crop_layout.visibility = View.VISIBLE
                    crop_image_view.setImageBitmap(copyBitmap)
                    crop_image_view.scaleType = ImageView.ScaleType.FIT_XY
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_CAMERA = 101
        private const val mOpenCvLibrary = "opencv_java4"

        @JvmField
        val allDraggedPointsStack = Stack<PolygonPoints>()

        init {
            System.loadLibrary(mOpenCvLibrary)
        }
    }

    override fun onClick(view: View) {
        val points = polygon_view.points
        val croppedBitmap: Bitmap?
        croppedBitmap = if (ScanUtils.isScanPointsValid(points)) {
            val point1 = Point(points[0]!!.x.toDouble(), points[0]!!.y.toDouble())
            val point2 = Point(points[1]!!.x.toDouble(), points[1]!!.y.toDouble())
            val point3 = Point(points[2]!!.x.toDouble(), points[2]!!.y.toDouble())
            val point4 = Point(points[3]!!.x.toDouble(), points[3]!!.y.toDouble())
            ScanUtils.enhanceReceipt(copyBitmap, point1, point2, point3, point4)
        } else {
            copyBitmap
        }
        val path = ScanUtils.saveToInternalMemory(croppedBitmap, ScanConstants.IMAGE_DIR,
                ScanConstants.IMAGE_NAME, this@ScanActivity, 90)[0]
        setResult(Activity.RESULT_OK, Intent().putExtra(ScanConstants.SCANNED_RESULT, path))
        //bitmap.recycle();
        System.gc()
        finish()
    }
}