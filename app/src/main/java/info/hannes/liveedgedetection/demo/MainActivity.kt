package info.hannes.liveedgedetection.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import info.hannes.github.AppUpdateHelper
import info.hannes.liveedgedetection.ScanConstants
import info.hannes.liveedgedetection.ScanUtils
import info.hannes.liveedgedetection.activity.ScanActivity
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startScan()

        AppUpdateHelper.checkForNewVersion(
                MainActivity@ this,
                BuildConfig.GIT_USER,
                BuildConfig.GIT_REPOSITORY,
                BuildConfig.VERSION_NAME
        )
    }

    private fun startScan() {
        val intent = Intent(this, ScanActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.let {
                    val filePath = it.getString(ScanConstants.SCANNED_RESULT)
                    val baseBitmap = ScanUtils.decodeBitmapFromFile(filePath, ScanConstants.IMAGE_NAME)
                    scanned_image.setImageBitmap(baseBitmap)
                    scanned_image.scaleType = ImageView.ScaleType.FIT_CENTER

                    Toast.makeText(this, filePath, Toast.LENGTH_LONG).show()
                    Timber.d(filePath)
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                finish()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 101
    }
}