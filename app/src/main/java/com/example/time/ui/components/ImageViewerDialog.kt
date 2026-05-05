/**
 * 图片预览对话框
 * 支持大图查看、缩放、下载、左右滑动切换
 * 使用改进的手势处理，解决滑动和缩放冲突
 */
package com.example.time.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.position
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Image
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 图片预览对话框
 * 
 * @param imagePaths 图片路径列表
 * @param initialIndex 初始显示的图片索引
 * @param onDismiss 关闭回调
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewerDialog(
    imagePaths: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (imagePaths.size - 1).coerceAtLeast(0)),
        pageCount = { imagePaths.size }
    )
    
    // 每张图片的缩放和偏移状态
    val scaleStates = remember { mutableStateListOf<Float>().apply { repeat(imagePaths.size) { add(1f) } } }
    val offsetXStates = remember { mutableStateListOf<Float>().apply { repeat(imagePaths.size) { add(0f) } } }
    val offsetYStates = remember { mutableStateListOf<Float>().apply { repeat(imagePaths.size) { add(0f) } } }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // 图片显示区域
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp, bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imagePaths.size > 1) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val path = imagePaths[page]
                        val bitmap = remember(path) { loadBitmapFromPath(path) }
                        
                        // 当前图片的缩放和偏移
                        val scale = scaleStates[page]
                        val offsetX = offsetXStates[page]
                        val offsetY = offsetYStates[page]
                        
                        // 图片加载完成后获取尺寸
                        var imageSize by remember { mutableStateOf(IntSize.Zero) }
                        
                        ZoomableImage(
                            bitmap = bitmap,
                            scale = scale,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            onScaleChange = { newScale ->
                                scaleStates[page] = newScale.coerceIn(1f, 5f)
                            },
                            onOffsetChange = { newOffsetX, newOffsetY ->
                                // 限制偏移范围，防止图片移出屏幕
                                val maxOffsetX = (imageSize.width * (scaleStates[page] - 1) / 2f)
                                val maxOffsetY = (imageSize.height * (scaleStates[page] - 1) / 2f)
                                offsetXStates[page] = newOffsetX.coerceIn(-maxOffsetX, maxOffsetX)
                                offsetYStates[page] = newOffsetY.coerceIn(-maxOffsetY, maxOffsetY)
                            },
                            onReset = {
                                scaleStates[page] = 1f
                                offsetXStates[page] = 0f
                                offsetYStates[page] = 0f
                            },
                            onImageSizeReady = { imageSize = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else if (imagePaths.size == 1) {
                    // 单张图片
                    val path = imagePaths.first()
                    val bitmap = remember(path) { loadBitmapFromPath(path) }
                    
                    var imageSize by remember { mutableStateOf(IntSize.Zero) }
                    
                    ZoomableImage(
                        bitmap = bitmap,
                        scale = scaleStates[0],
                        offsetX = offsetXStates[0],
                        offsetY = offsetYStates[0],
                        onScaleChange = { scaleStates[0] = it.coerceIn(1f, 5f) },
                        onOffsetChange = { x, y ->
                            val maxOffsetX = (imageSize.width * (scaleStates[0] - 1) / 2f)
                            val maxOffsetY = (imageSize.height * (scaleStates[0] - 1) / 2f)
                            offsetXStates[0] = x.coerceIn(-maxOffsetX, maxOffsetX)
                            offsetYStates[0] = y.coerceIn(-maxOffsetY, maxOffsetY)
                        },
                        onReset = {
                            scaleStates[0] = 1f
                            offsetXStates[0] = 0f
                            offsetYStates[0] = 0f
                        },
                        onImageSizeReady = { imageSize = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // 顶部工具栏
            TopAppBar(
                currentPage = pagerState.currentPage + 1,
                totalPages = imagePaths.size,
                onClose = onDismiss,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            
            // 底部工具栏
            val currentPath = imagePaths.getOrElse(pagerState.currentPage) { imagePaths.firstOrNull() } ?: ""
            BottomToolbar(
                imagePath = currentPath,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            
            // 底部页码指示器
            if (imagePaths.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 72.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(imagePaths.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                                .background(
                                    if (index == pagerState.currentPage) Color.White else Color.White.copy(alpha = 0.4f),
                                    androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * 可缩放的图片组件
 * 支持双指缩放、拖拽、双击重置
 * 当缩放为1时，允许HorizontalPager处理滑动
 */
@Composable
fun ZoomableImage(
    bitmap: Bitmap?,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Float, Float) -> Unit,
    onReset: () -> Unit,
    onImageSizeReady: (IntSize) -> Unit,
    modifier: Modifier = Modifier
) {
    // 用于追踪双指手势
    var lastTouchDistance by remember { mutableFloatStateOf(0f) }
    var lastTouchCenter by remember { mutableStateOf(Offset.Zero) }
    var lastOffsetX by remember { mutableFloatStateOf(0f) }
    var lastOffsetY by remember { mutableFloatStateOf(0f) }
    var currentScale by remember { mutableFloatStateOf(1f) }
    var isZooming by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { size -> onImageSizeReady(size) }
            .pointerInput(Unit) {
                // 自定义手势处理
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        
                        val pointers = event.changes.filter { it.pressed }
                        
                        when (pointers.size) {
                            2 -> {
                                // 双指手势（缩放/拖拽）
                                val p1 = pointers[0].position
                                val p2 = pointers[1].position
                                
                                val currentDistance = sqrt(
                                    (p2.x - p1.x) * (p2.x - p1.x) + 
                                    (p2.y - p1.y) * (p2.y - p1.y)
                                )
                                
                                val center = Offset((p1.x + p2.x) / 2, (p1.y + p2.y) / 2)
                                
                                if (lastTouchDistance > 0) {
                                    // 计算缩放比例
                                    val scaleFactor = currentDistance / lastTouchDistance
                                    currentScale = (currentScale * scaleFactor).coerceIn(1f, 5f)
                                    onScaleChange(currentScale)
                                    
                                    if (currentScale > 1.01f) {
                                        isZooming = true
                                        // 计算拖拽偏移
                                        val dx = center.x - lastTouchCenter.x
                                        val dy = center.y - lastTouchCenter.y
                                        onOffsetChange(lastOffsetX + dx, lastOffsetY + dy)
                                        lastOffsetX += dx
                                        lastOffsetY += dy
                                    }
                                }
                                
                                lastTouchDistance = currentDistance
                                lastTouchCenter = center
                                
                                // 消费事件，阻止传递给Pager
                                pointers.forEach { it.consume() }
                            }
                            1 -> {
                                // 单指手势
                                if (isZooming && currentScale > 1.01f) {
                                    // 缩放状态下的单指拖拽
                                    val pos = pointers[0].position
                                    val dx = pos.x - lastTouchCenter.x
                                    val dy = pos.y - lastTouchCenter.y
                                    onOffsetChange(lastOffsetX + dx, lastOffsetY + dy)
                                    lastOffsetX += dx
                                    lastOffsetY += dy
                                    lastTouchCenter = pos
                                    pointers[0].consume()
                                } else {
                                    // 非缩放状态，不消费，让Pager处理
                                    isZooming = false
                                    lastTouchDistance = 0f
                                }
                            }
                            0 -> {
                                // 没有触摸，重置状态
                                lastTouchDistance = 0f
                                if (currentScale <= 1.01f) {
                                    isZooming = false
                                    lastOffsetX = 0f
                                    lastOffsetY = 0f
                                }
                            }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // 双击重置或放大到2x
                        if (currentScale > 1.5f) {
                            onReset()
                            currentScale = 1f
                            lastOffsetX = 0f
                            lastOffsetY = 0f
                            isZooming = false
                        } else {
                            currentScale = 2f
                            onScaleChange(2f)
                            isZooming = true
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "预览图片",
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    },
                contentScale = ContentScale.Fit
            )
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("📷", color = Color.White)
        }
    }
}

/**
 * 顶部工具栏
 */
@Composable
fun TopAppBar(
    currentPage: Int,
    totalPages: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 页码
        Text(
            text = "$currentPage / $totalPages",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
        
        // 关闭按钮
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "关闭",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * 底部工具栏
 */
@Composable
fun BottomToolbar(
    imagePath: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 下载按钮
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                saveImageToGallery(context, imagePath)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "下载",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("保存", color = Color.White, style = MaterialTheme.typography.bodySmall)
        }
        
        // 分享按钮
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                shareImage(context, imagePath)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "分享",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("分享", color = Color.White, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * 从路径加载图片
 */
private fun loadBitmapFromPath(path: String?): Bitmap? {
    return try {
        when {
            path == null -> null
            path.startsWith("/") -> BitmapFactory.decodeFile(path)
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * 保存图片到相册
 */
private fun saveImageToGallery(context: android.content.Context, path: String) {
    try {
        val sourceFile = File(path)
        if (!sourceFile.exists()) {
            Toast.makeText(context, "图片不存在", Toast.LENGTH_SHORT).show()
            return
        }
        
        val contentResolver = context.contentResolver
        val fileName = "Timefly_${System.currentTimeMillis()}_${sourceFile.name}"
        
        // 使用 MediaStore 保存到公共相册
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "${android.os.Environment.DIRECTORY_PICTURES}/Timefly")
            put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
        }
        
        val imageUri = contentResolver.insert(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        
        if (imageUri == null) {
            Toast.makeText(context, "创建相册条目失败", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 复制图片数据
        contentResolver.openOutputStream(imageUri)?.use { outputStream ->
            sourceFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: run {
            Toast.makeText(context, "写入图片失败", Toast.LENGTH_SHORT).show()
            contentResolver.delete(imageUri, null, null)
            return
        }
        
        // 标记为已完成，使其在相册中可见
        contentValues.clear()
        contentValues.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
        contentResolver.update(imageUri, contentValues, null, null)
        
        Toast.makeText(context, "已保存到相册 Pictures/Timefly", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}

/**
 * 分享图片
 */
private fun shareImage(context: android.content.Context, path: String) {
    try {
        val file = File(path)
        if (!file.exists()) {
            Toast.makeText(context, "图片不存在", Toast.LENGTH_SHORT).show()
            return
        }
        
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(android.content.Intent.createChooser(intent, "分享图片"))
    } catch (e: Exception) {
        Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}