package ru.kyamshanov.notepen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DetailsContent(component: DetailsComponent, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(Color.LightGray)) {
        val model by component.model.subscribeAsState()
        val filePath = remember(model.title) { model.title }
        val pdfManager = remember(filePath) { PdfManager(filePath) }
        val metadata = remember(pdfManager.metadata) { pdfManager.metadata }
        val pages = remember(metadata.pages) { metadata.pages }
        val pagesCache = remember(pages) { mutableMapOf<Int, ImageBitmap>() }

        DisposableEffect(pdfManager) {
            onDispose {
                pdfManager.close()
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            items(items = pages, { it.pageNumber }) { page ->
                val windowInfo = LocalWindowInfo.current
                val screenWidth = windowInfo.containerSize.width
                val targetWidth = screenWidth * 2f / 3f

                val aspectRatio = page.aspectRatio
                val targetHeight = targetWidth / aspectRatio

                val width = with(LocalDensity.current) { targetWidth.toDp() }
                val height = with(LocalDensity.current) { targetHeight.toDp() }

                Box(modifier = Modifier.size(width, height)) {
                    val pageIndex = page.pageNumber
                    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
                    LaunchedEffect(null) {
                        withContext(Dispatchers.IO) {
                            imageBitmap = pagesCache[pageIndex] ?: pdfManager.renderPage(pageIndex)
                                ?.also { pagesCache[pageIndex] = it }
                        }
                    }

                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap!!,
                            contentDescription = "PDF страница $pageIndex",
                            modifier = Modifier.size(width, height),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("Loading")
                    }
                }
            }
        }
    }
}
