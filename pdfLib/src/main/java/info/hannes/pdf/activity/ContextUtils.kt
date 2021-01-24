package info.hannes.pdf.activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfVersion
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import timber.log.Timber
import java.io.File


fun Context.viewPdf(pdfFile: File) {
    val path: Uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", pdfFile);

    // Setting the intent for pdf reader
    val pdfIntent = Intent(Intent.ACTION_VIEW)
    pdfIntent.setDataAndType(path, "application/pdf")
    pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    try {
        startActivity(pdfIntent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "Can't read pdf file", Toast.LENGTH_SHORT).show()
    }
}

fun File.createPdf(path: File): File {

    val file = File("$path${File.pathSeparatorChar}iText_Image_Example.pdf")
    val pdf = PdfDocument(
            PdfWriter(file.absoluteFile.absolutePath,
                    WriterProperties()
                            .addXmpMetadata()
                            .setPdfVersion(PdfVersion.PDF_1_6)))
    val info = pdf.documentInfo
    info.title = "The Strange Case of Dr. Jekyll and Mr. Hyde"
    info.author = "Robert Louis Stevenson"
    info.subject = "A novel"
    info.keywords = "Dr. Jekyll, Mr. Hyde"
    info.creator = "A simple tutorial example"
    val document = Document(pdf)
    try {
        val imageData = ImageDataFactory.create(this.absolutePath)
        val pdfImg = Image(imageData)
        document.add(pdfImg)
    } catch (e: Exception) {
        Timber.e(e)
    } finally {
        document.close()
    }
    return file
}