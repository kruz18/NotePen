package ru.kyamshanov.notepen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun DetailsContent(component: DetailsComponent, modifier: Modifier = Modifier) {
    val model by component.model.subscribeAsState()
    val filePath = model.title
    val pdfManager = remember(filePath) { PdfManager(filePath) }
    var currentPage by remember { mutableStateOf(1) }
    var scale by remember { mutableStateOf(1f) }

    DisposableEffect(pdfManager) {
        onDispose {
            pdfManager.close()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Панель управления
        /*
        PdfControls(
            currentPage = currentPage,
            pageCount = pdfManager.metadata.pageCount,
            onPageChange = { currentPage = it },
            scale = scale,
            onScaleChange = { scale = it }
        )
        */

        // Отображение PDF
        PdfDocumentView(
            pdfManager = pdfManager,
            currentPage = currentPage,
            onPageChange = { currentPage = it },
            scale = scale,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )
    }
}


/**
 * Composable функция для отображения многостраничного PDF с ленивой загрузкой при скролле
 * Использует LazyColumn для корректной работы с constraints
 */
@Composable
fun PdfDocumentView(
    pdfManager: PdfManager,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    scale: Float = 1f,
    preloadPages: Int = 2,
    modifier: Modifier = Modifier
) {
    val pageCount = pdfManager.metadata.pageCount
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = currentPage)
    var loadedPages by remember { mutableStateOf(setOf<Int>()) }
    var pageHeights by remember { mutableStateOf(mapOf<Int, Dp>()) }

    // Отслеживаем текущую видимую страницу
    val visiblePage by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                layoutInfo.visibleItemsInfo.first().index
            } else {
                currentPage
            }
        }
    }

    // Обновляем текущую страницу при скролле
    LaunchedEffect(visiblePage) {
        if (visiblePage != currentPage) {
            onPageChange(visiblePage)
        }
    }

    // Функция для загрузки страницы
    suspend fun loadPage(page: Int) {
        if (page in 0 until pageCount && page !in loadedPages) {
            delay(if (page == currentPage) 0L else 100L)
            pdfManager.renderPage(page, scale)
            loadedPages = loadedPages + page
        }
    }

    // Загружаем текущую страницу и соседние
    LaunchedEffect(currentPage, scale) {
        loadPage(currentPage)

        for (i in 1..preloadPages) {
            loadPage(currentPage + i)
            loadPage(currentPage - i)
        }
    }

    // Предзагрузка страниц при приближении к ним
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress) {
            val layoutInfo = lazyListState.layoutInfo
            val visibleIndices = layoutInfo.visibleItemsInfo.map { it.index }
            val lastVisible = visibleIndices.maxOrNull() ?: currentPage

            // Загружаем следующие страницы
            for (i in 1..preloadPages) {
                val nextPage = lastVisible + i
                if (nextPage < pageCount) {
                    loadPage(nextPage)
                }
            }

            // Загружаем предыдущие страницы
            val firstVisible = visibleIndices.minOrNull() ?: currentPage
            for (i in 1..preloadPages) {
                val prevPage = firstVisible - i
                if (prevPage >= 0) {
                    loadPage(prevPage)
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(pageCount) { page ->
                val pageInfo = pdfManager.metadata.pages.getOrNull(page)
                val aspectRatio = pageInfo?.let { it.width / it.height } ?: 1.4f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio)
                        .onSizeChanged { size ->
                            // Сохраняем высоту страницы для стабильного layout
                            pageHeights = pageHeights + (page to size.height.dp)
                        }
                ) {
                    PdfPageView(
                        pdfManager = pdfManager,
                        pageIndex = page,
                        scale = scale,
                        modifier = Modifier.fillMaxSize(),
                        loadingIndicator = {
                            if (page != currentPage) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(strokeWidth = 1.dp)
                                }
                            }
                        }
                    )
                }
            }
        }

        // Индикатор текущей страницы
        PageIndicator(
            currentPage = currentPage + 1,
            totalPages = pageCount,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
    }
}

// Простой индикатор страницы
@Composable
fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$currentPage / $totalPages",
            color = Color.White,
            style = MaterialTheme.typography.caption
        )
    }
}

/**
 * Composable функция для отображения одной страницы PDF с поддержкой скроллинга
 *
 * @param pdfManager менеджер PDF документа
 * @param pageIndex индекс отображаемой страницы (0-based)
 * @param scale масштаб отображения (1f = 100%)
 * @param modifier модификатор для компоновки
 * @param loadingIndicator индикатор загрузки (опционально)
 * @param errorContent контент для отображения ошибки (опционально)
 */
@Composable
fun PdfPageView(
    pdfManager: PdfManager,
    pageIndex: Int,
    scale: Float = 1f,
    modifier: Modifier = Modifier,
    loadingIndicator: @Composable (() -> Unit)? = null,
    errorContent: @Composable ((Throwable?) -> Unit)? = null
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Throwable?>(null) }

    val scrollState = rememberScrollState()

    // Загрузка страницы при изменении pageIndex или scale
    LaunchedEffect(pageIndex, scale) {
        if (pageIndex < 0 || pageIndex >= pdfManager.metadata.pageCount) {
            error = IndexOutOfBoundsException("Страница $pageIndex не существует")
            return@LaunchedEffect
        }

        isLoading = true
        error = null

        try {
            val renderedImage = withContext(Dispatchers.IO) {
                pdfManager.renderPage(pageIndex, scale)
            }
            imageBitmap = renderedImage
        } catch (e: Exception) {
            error = e
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .background(Color.LightGray)
            .verticalScroll(scrollState)
    ) {
        when {
            isLoading -> {
                // Отображение индикатора загрузки
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    loadingIndicator?.invoke() ?: CircularProgressIndicator()
                }
            }

            error != null -> {
                // Отображение ошибки
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    errorContent?.invoke(error) ?: DefaultErrorContent(error)
                }
            }

            imageBitmap != null -> {
                // Отображение страницы PDF
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = "PDF страница ${pageIndex + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            else -> {
                // Пустое состояние
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun DefaultErrorContent(error: Throwable?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ошибка загрузки страницы",
            color = Color.Red,
            style = MaterialTheme.typography.h6
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = error?.message ?: "Неизвестная ошибка",
            color = Color.Gray,
            style = MaterialTheme.typography.body2
        )
    }
}
