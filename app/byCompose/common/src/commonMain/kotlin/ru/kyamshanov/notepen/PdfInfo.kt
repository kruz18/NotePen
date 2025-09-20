package ru.kyamshanov.notepen

/**
 * Полная информация о PDF документе
 *
 * @param filePath путь к файлу PDF
 * @param pageCount общее количество страниц в документе
 * @param pages список информации о каждой странице
 * @param title название документа из метаданных
 * @param author автор документа из метаданных
 * @param creator программа-создатель документа из метаданных
 * @param creationDate дата создания документа в строковом формате
 * @param modificationDate дата последнего изменения документа
 * @param isEncrypted флаг, указывающий на наличие защиты паролем
 * @param hasUniformPageSizes флаг, указывающий что все страницы одинакового размера
 */
data class PdfInfo(
    val filePath: String,
    val pageCount: Int,
    val pages: List<PdfPageInfo>,
    val title: String? = null,
    val author: String? = null,
    val creator: String? = null,
    val creationDate: String? = null,
    val modificationDate: String? = null,
    val isEncrypted: Boolean = false,
    val hasUniformPageSizes: Boolean
) {
    /**
     * Получает размеры первой страницы для быстрого доступа
     * @return Pair<ширина, высота> первой страницы или null если страниц нет
     */
    val firstPageDimensions: Pair<Float, Float>?
        get() = pages.firstOrNull()?.let { it.width to it.height }

    /**
     * Проверяет, содержит ли документ страницы разных размеров
     * @return true если размеры страниц не одинаковы
     */
    val hasMixedPageSizes: Boolean get() = !hasUniformPageSizes

    /**
     * Получает уникальные размеры страниц в документе
     * @return множество уникальных пар размеров (ширина, высота)
     */
    val uniquePageSizes: Set<Pair<Float, Float>>
        get() = pages.map { it.width to it.height }.toSet()

    /**
     * Находит наиболее часто встречающийся размер страниц
     * @return самый распространённый размер или null если страниц нет
     */
    val mostCommonPageSize: Pair<Float, Float>?
        get() = pages.groupingBy { it.width to it.height }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
}