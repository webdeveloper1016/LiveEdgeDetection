package info.hannes.liveedgedetection

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    fun saveToInternalMemory(bitmap: Bitmap, fileDirectory: String, fileName: String, context: Context, quality: Int): Array<String?> {
        val returnParams = arrayOfNulls<String>(2)
        val directory = getBaseDirectoryFromPathString(fileDirectory, context)
        val path = File(directory, fileName)
        try {
            val fileOutputStream = FileOutputStream(path)
            //Compress method used on the Bitmap object to write  image to output stream
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream)
            fileOutputStream.close()
        } catch (e: Exception) {
            Timber.e(e)
        }
        returnParams[0] = directory.absolutePath
        returnParams[1] = fileName
        return returnParams
    }

    private fun getBaseDirectoryFromPathString(mPath: String, context: Context): File {
        val contextWrapper = ContextWrapper(context)
        return contextWrapper.getDir(mPath, Context.MODE_PRIVATE)
    }

    fun decodeBitmapFromFile(path: String, imageName: String): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        return BitmapFactory.decodeFile(File(path, imageName).absolutePath, options)
    }

    fun decodeBitmapFromByteArray(data: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap {
        // Raw height and width of image
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(data, 0, data.size, options)

        // Calculate inSampleSize
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        options.inSampleSize = inSampleSize

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(data, 0, data.size, options)
    }

    @Deprecated("")
    fun loadEfficientBitmap(data: ByteArray, width: Int, height: Int): Bitmap {
        val bmp: Bitmap

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(data, 0, data.size, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        bmp = BitmapFactory.decodeByteArray(data, 0, data.size, options)
        return bmp
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}