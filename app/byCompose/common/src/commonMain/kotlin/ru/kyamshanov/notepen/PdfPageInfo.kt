package ru.kyamshanov.notepen

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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

/**
 * Расширения для работы с размерами в dp
 */

val PdfPageInfo.widthDp: Dp
    @Composable
    get() = with(LocalDensity.current) {
        (width / 72f * 160).toDp() // points -> inches -> dp (1 inch = 160 dp)
    }

val PdfPageInfo.heightDp: Dp
    @Composable
    get() = with(LocalDensity.current) {
        (height / 72f * 160).toDp()
    }

val PdfPageInfo.effectiveWidthDp: Dp
    @Composable
    get() = with(LocalDensity.current) {
        val (effectiveWidth, _) = effectiveDimensions
        (effectiveWidth / 72f * 160).toDp()
    }

val PdfPageInfo.effectiveHeightDp: Dp
    @Composable
    get() = with(LocalDensity.current) {
        val (_, effectiveHeight) = effectiveDimensions
        (effectiveHeight / 72f * 160).toDp()
    }

val PdfPageInfo.sizeDp: Pair<Dp, Dp>
    @Composable
    get() = widthDp to heightDp

val PdfPageInfo.effectiveSizeDp: Pair<Dp, Dp>
    @Composable
    get() = effectiveWidthDp to effectiveHeightDp