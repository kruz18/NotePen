package ru.kyamshanov.notepen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DetailsContent(component: DetailsComponent, modifier: Modifier = Modifier) {
    val model by component.model.subscribeAsState()
    val filePath = model.title
    val pdfManager = remember { PdfManager(filePath) }
    val pageCount = remember { pdfManager.getPageCount() }
    var currentPage by remember { mutableStateOf(0) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Загрузка страницы
    LaunchedEffect(currentPage) {
        imageBitmap = withContext(Dispatchers.IO) {
            pdfManager.renderPage(currentPage)
        }
    }

    // Состояние для вертикального скроллинга
    var offsetY by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollableState { delta ->
        offsetY += delta
        delta
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {


        // Область просмотра PDF с скроллингом
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.Gray)
                .scrollable(
                    state = scrollState,
                    orientation = Orientation.Vertical
                )
                .pointerInput(Unit) {
                    awaitEachGesture {
                        // Обработка жестов прокрутки
                    }
                }
        ) {
            imageBitmap?.let { bitmap ->
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    val imageHeight = bitmap.height.toFloat()
                    val imageWidth = bitmap.width.toFloat()

                    // Масштабируем изображение под ширину экрана
                    val scale = size.width / imageWidth
                    val scaledHeight = imageHeight * scale

                    drawImage(
                        image = bitmap,
                        dstOffset = IntOffset(0, -offsetY.toInt()),
                        dstSize = IntSize(size.width.toInt(), scaledHeight.toInt())
                    )
                }
            }

           /* VerticalScrollbar(
                Modifier.align(Alignment.CenterEnd),
                scrollState
            )*/
        }
    }

    // Очистка при выходе
    DisposableEffect(Unit) {
        onDispose {
            pdfManager.close()
        }
    }
}