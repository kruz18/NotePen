package ru.kyamshanov.notepen

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize

interface PdfManager {

    val metadata: PdfInfo

    fun renderPage(pageIndex: Int, viewSize: IntSize): ImageBitmap?
    fun close()
}