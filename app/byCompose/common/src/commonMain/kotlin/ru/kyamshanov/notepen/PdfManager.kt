package ru.kyamshanov.notepen

import androidx.compose.ui.graphics.ImageBitmap

interface PdfManager {

    val metadata: PdfInfo

    fun renderPage(pageIndex: Int, scale: Float = 1f): ImageBitmap?
    fun close()
}