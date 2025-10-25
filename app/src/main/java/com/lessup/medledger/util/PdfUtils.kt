package com.lessup.medledger.util

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object PdfUtils {
    fun imagesToPdf(context: Context, imagePaths: List<String>, fileNamePrefix: String = "scan"): String {
        require(imagePaths.isNotEmpty()) { "imagePaths is empty" }
        val pdf = PdfDocument()
        try {
            imagePaths.forEachIndexed { index, path ->
                val bmp = BitmapFactory.decodeFile(path)
                val pageInfo = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, index + 1).create()
                val page = pdf.startPage(pageInfo)
                page.canvas.drawBitmap(bmp, 0f, 0f, null)
                pdf.finishPage(page)
            }
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            val name = fileNamePrefix + "_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".pdf"
            val outFile = File(dir, name)
            FileOutputStream(outFile).use { pdf.writeTo(it) }
            return outFile.absolutePath
        } finally {
            pdf.close()
        }
    }
}
