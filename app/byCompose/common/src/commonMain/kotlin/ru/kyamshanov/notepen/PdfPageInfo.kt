package ru.kyamshanov.notepen

/**
 * Информация об отдельной странице PDF документа
 *
 * @param pageNumber номер страницы (начинается с 0)
 * @param width ширина страницы в пунктах (points), 1 пункт = 1/72 дюйма
 * @param height высота страницы в пунктах (points)
 * @param rotation угол поворота страницы в градусах (0, 90, 180, 270)
 */
data class PdfPageInfo(
    val pageNumber: Int,
    val width: Float,
    val height: Float,
    val rotation: Int = 0
) {
    /**
     * Проверяет, является ли страница альбомной ориентации
     * @return true если ширина больше высоты
     */
    val isLandscape: Boolean get() = width > height

    /**
     * Проверяет, является ли страница портретной ориентации
     * @return true если высота больше или равна ширине
     */
    val isPortrait: Boolean get() = height >= width

    /**
     * Вычисляет соотношение сторон страницы
     * @return отношение ширины к высоте (width / height)
     */
    val aspectRatio: Float get() = width / height

    /**
     * Получает эффективные размеры с учётом поворота
     * @return Pair<эффективная ширина, эффективная высота>
     */
    val effectiveDimensions: Pair<Float, Float>
        get() = when (rotation) {
            90, 270 -> height to width // поворот меняет ориентацию
            else -> width to height
        }
}