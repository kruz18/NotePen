package ru.kyamshanov.notepen

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.io.File
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.apache.pdfbox.Loader
import java.awt.image.BufferedImage

class PdfManagerJvm(private val filePath: String) : PdfManager {
    private val document: PDDocument
    private val renderer: PDFRenderer

    init {
        try {
            document = Loader.loadPDF(File(filePath))
            renderer = PDFRenderer(document)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override val metadata: PdfInfo by lazy { loadPdfInfo() }

    /**
     * Загружает и парсит информацию о PDF документе
     * @return полная информация о PDF
     * @throws IllegalArgumentException если файл не существует или не является PDF
     * @throws SecurityException если нет прав доступа к файлу
     */
    private fun loadPdfInfo(): PdfInfo {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                throw IllegalArgumentException("Файл не существует: $filePath")
            }
            if (!file.canRead()) {
                throw SecurityException("Нет прав на чтение файла: $filePath")
            }

            val documentInfo = document.documentInformation

            // Получаем информацию о всех страницах
            val pagesInfo = document.pages.mapIndexed { index, page ->
                val mediaBox = page.mediaBox
                PdfPageInfo(
                    pageNumber = index,
                    width = mediaBox.width,
                    height = mediaBox.height,
                    rotation = page.rotation
                )
            }

            // Проверяем одинаковы ли размеры всех страниц
            val hasUniformSizes = pagesInfo.map { it.width to it.height }
                .toSet()
                .size == 1

            return PdfInfo(
                filePath = filePath,
                pageCount = document.numberOfPages,
                pages = pagesInfo,
                title = documentInfo.title,
                author = documentInfo.author,
                creator = documentInfo.creator,
                creationDate = documentInfo.creationDate?.toString(),
                modificationDate = documentInfo.modificationDate?.toString(),
                isEncrypted = document.isEncrypted,
                hasUniformPageSizes = hasUniformSizes
            )

        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException, is SecurityException -> throw e
                else -> throw IllegalArgumentException("Ошибка чтения PDF файла: ${e.message}", e)
            }
        }
    }

    override fun renderPage(pageIndex: Int, scale: Float): ImageBitmap? {
        return try {
            if (pageIndex in 0 until metadata.pageCount) {
                val bufferedImage: BufferedImage = renderer.renderImage(pageIndex, scale)
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
        document.close()
    }
}
