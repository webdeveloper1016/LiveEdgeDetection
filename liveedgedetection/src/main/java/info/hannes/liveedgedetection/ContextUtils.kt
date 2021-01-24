package info.hannes.liveedgedetection

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File


fun Context.storeBitmap(bitmap: Bitmap, file: File) {
    getUriForFile(file).run {
        contentResolver.openOutputStream(this)?.run {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
            close()
        }
    }
}

fun Context.getUriForFile(file: File): Uri =
        FileProvider.getUriForFile(this, "$packageName.provider", file)
