package ru.kyamshanov.notepen

import androidx.compose.ui.graphics.ImageBitmap

interface PdfManager {
    fun getPageCount(): Int
    fun renderPage(pageIndex: Int, scale: Float = 1f): ImageBitmap?
    fun close()
}