package ru.kyamshanov.notepen

private const val NORMAL_DPI = 300f

actual fun PdfManager(path: String): PdfManager =
    PdfManagerJvm(path)