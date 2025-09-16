package ru.kyamshanov.notepen

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.io.File
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.apache.pdfbox.Loader
import java.awt.image.BufferedImage

class PdfManagerJvm(private val filePath: String) : PdfManager {
    private var document: PDDocument? = null
    private var renderer: PDFRenderer? = null

    init {
        loadDocument()
    }

    private fun loadDocument() {
        try {
            document = Loader.loadPDF(File(filePath))
            renderer = PDFRenderer(document)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getPageCount(): Int {
        return document?.numberOfPages ?: 0
    }

    override fun renderPage(pageIndex: Int, scale: Float): ImageBitmap? {
        return try {
            if (pageIndex in 0 until getPageCount()) {
                val bufferedImage: BufferedImage = renderer!!.renderImage(pageIndex, scale)
                bufferedImage.toComposeImageBitmap()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun close() {
        document?.close()
    }
}
