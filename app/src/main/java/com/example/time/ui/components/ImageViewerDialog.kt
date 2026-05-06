/**
 * 图片预览对话框
 * 支持大图查看、缩放、下载、左右滑动切换
 */
package com.example.time.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Image
import java.io.File
import kotlin.math.sqrt

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
    
    val scaleStates = remember { mutableStateListOf<Float>().apply { repeat(imagePaths.size) { add(1f) } } }
    val offsetXStates = remember { mutableStateListOf<Float>().apply { repeat(imagePaths.size) { add(0f) } } }
    val offsetYStates = remember { mutableStateListOf<Float>().apply { repeat(imagePaths.size) { add(0f) } } }
    
    var isZoomedGlobally by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f))) {
            Box(
                modifier = Modifier.fillMaxSize().padding(top = 60.dp, bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imagePaths.size > 1) {
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = !isZoomedGlobally,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val path = imagePaths[page]
                        val bitmap = remember(path) { loadBitmapFromPath(path) }
                        var imageSize by remember { mutableStateOf(IntSize.Zero) }
                        
                        ZoomableImage(
                            bitmap = bitmap,
                            scale = scaleStates[page],
                            offsetX = offsetXStates[page],
                            offsetY = offsetYStates[page],
                            onScaleChange = { newScale, newOffsetX, newOffsetY ->
                                val finalScale = if (newScale < 1.05f) 1f else newScale.coerceIn(1f, 5f)
                                scaleStates[page] = finalScale
                                val maxOffsetX = (imageSize.width * (finalScale - 1) / 2f)
                                val maxOffsetY = (imageSize.height * (finalScale - 1) / 2f)
                                offsetXStates[page] = if (finalScale <= 1f) 0f else newOffsetX.coerceIn(-maxOffsetX, maxOffsetX)
                                offsetYStates[page] = if (finalScale <= 1f) 0f else newOffsetY.coerceIn(-maxOffsetY, maxOffsetY)
                                isZoomedGlobally = finalScale > 1.01f
                            },
                            onImageSizeReady = { imageSize = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else if (imagePaths.size == 1) {
                    val path = imagePaths.first()
                    val bitmap = remember(path) { loadBitmapFromPath(path) }
                    var imageSize by remember { mutableStateOf(IntSize.Zero) }
                    
                    ZoomableImage(
                        bitmap = bitmap,
                        scale = scaleStates[0],
                        offsetX = offsetXStates[0],
                        offsetY = offsetYStates[0],
                        onScaleChange = { newScale, newOffsetX, newOffsetY ->
                            val finalScale = if (newScale < 1.05f) 1f else newScale.coerceIn(1f, 5f)
                            scaleStates[0] = finalScale
                            val maxOffsetX = (imageSize.width * (finalScale - 1) / 2f)
                            val maxOffsetY = (imageSize.height * (finalScale - 1) / 2f)
                            offsetXStates[0] = if (finalScale <= 1f) 0f else newOffsetX.coerceIn(-maxOffsetX, maxOffsetX)
                            offsetYStates[0] = if (finalScale <= 1f) 0f else newOffsetY.coerceIn(-maxOffsetY, maxOffsetY)
                        },
                        onImageSizeReady = { imageSize = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            TopAppBar(
                currentPage = pagerState.currentPage + 1,
                totalPages = imagePaths.size,
                onClose = onDismiss,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            
            val currentPath = imagePaths.getOrElse(pagerState.currentPage) { imagePaths.firstOrNull() } ?: ""
            BottomToolbar(imagePath = currentPath, modifier = Modifier.align(Alignment.BottomCenter))
            
            if (imagePaths.size > 1) {
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp),
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
 * 关键：双指时消费事件，单指未缩放时不消费（让 Pager 处理）
 */
@Composable
fun ZoomableImage(
    bitmap: Bitmap?,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onScaleChange: (Float, Float, Float) -> Unit,
    onImageSizeReady: (IntSize) -> Unit,
    modifier: Modifier = Modifier
) {
    // 追踪所有按下的指针
    var pressedPointers by remember { mutableStateOf<Map<PointerId, Offset>>(emptyMap()) }
    
    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { size -> onImageSizeReady(size) }
            .pointerInput(Unit) {
                awaitEachGesture {
                    // 等待第一个手指
                    val firstDown = awaitFirstDown()
                    pressedPointers = mapOf(firstDown.id to firstDown.position)
                    
                    var lastDistance = 0f
                    var lastCenter = Offset.Zero
                    var lastOneFingerPos = firstDown.position
                    
                    // 持续处理手势
                    while (true) {
                        val event = awaitPointerEvent()
                        
                        // 更新按下的指针
                        val newPressedPointers = mutableMapOf<PointerId, Offset>()
                        for (change in event.changes) {
                            if (change.pressed) {
                                newPressedPointers[change.id] = change.position
                            }
                        }
                        pressedPointers = newPressedPointers
                        
                        if (newPressedPointers.isEmpty()) break // 所有手指抬起，结束
                        
                        when {
                            newPressedPointers.size >= 2 -> {
                                // 双指：处理缩放
                                val positions = newPressedPointers.values.toList()
                                val p1 = positions[0]
                                val p2 = positions[1]
                                
                                val distance = sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y))
                                val center = Offset((p1.x + p2.x) / 2, (p1.y + p2.y) / 2)
                                
                                if (lastDistance > 0) {
                                    val zoom = distance / lastDistance
                                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                                    val pan = Offset(center.x - lastCenter.x, center.y - lastCenter.y)
                                    onScaleChange(newScale, offsetX + pan.x, offsetY + pan.y)
                                }
                                
                                lastDistance = distance
                                lastCenter = center
                                
                                // 双指时消费事件
                                event.changes.forEach { if (it.pressed) it.consume() }
                            }
                            newPressedPointers.size == 1 && scale > 1.01f -> {
                                // 单指 + 已缩放：处理拖拽
                                val pos = newPressedPointers.values.first()
                                val pan = Offset(pos.x - lastOneFingerPos.x, pos.y - lastOneFingerPos.y)
                                onScaleChange(scale, offsetX + pan.x, offsetY + pan.y)
                                lastOneFingerPos = pos
                                lastCenter = pos
                                
                                // 消费事件
                                event.changes.forEach { if (it.pressed) it.consume() }
                            }
                            else -> {
                                // 单指 + 未缩放：不消费，让 Pager 处理
                                lastOneFingerPos = newPressedPointers.values.first()
                            }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.01f) {
                            onScaleChange(1f, 0f, 0f)
                        } else {
                            onScaleChange(2f, 0f, 0f)
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
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                },
                contentScale = ContentScale.Fit
            )
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("📷", color = Color.White)
        }
    }
}

@Composable
fun TopAppBar(currentPage: Int, totalPages: Int, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$currentPage / $totalPages", color = Color.White, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onClose) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun BottomToolbar(imagePath: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { saveImageToGallery(context, imagePath) }) {
            Icon(imageVector = Icons.Default.Download, contentDescription = "下载", tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text("保存", color = Color.White, style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { shareImage(context, imagePath) }) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "分享", tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text("分享", color = Color.White, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun loadBitmapFromPath(path: String?): Bitmap? {
    return try {
        when {
            path == null -> null
            path.startsWith("/") -> BitmapFactory.decodeFile(path)
            else -> null
        }
    } catch (e: Exception) { null }
}

private fun saveImageToGallery(context: android.content.Context, path: String) {
    try {
        val sourceFile = File(path)
        if (!sourceFile.exists()) { Toast.makeText(context, "图片不存在", Toast.LENGTH_SHORT).show(); return }
        val contentResolver = context.contentResolver
        val fileName = "Timefly_${System.currentTimeMillis()}_${sourceFile.name}"
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "${android.os.Environment.DIRECTORY_PICTURES}/Timefly")
            put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
        }
        val imageUri = contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri == null) { Toast.makeText(context, "创建相册条目失败", Toast.LENGTH_SHORT).show(); return }
        contentResolver.openOutputStream(imageUri)?.use { outputStream -> sourceFile.inputStream().use { inputStream -> inputStream.copyTo(outputStream) } }
            ?: run { Toast.makeText(context, "写入图片失败", Toast.LENGTH_SHORT).show(); contentResolver.delete(imageUri, null, null); return }
        contentValues.clear(); contentValues.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
        contentResolver.update(imageUri, contentValues, null, null)
        Toast.makeText(context, "已保存到相册 Pictures/Timefly", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) { Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show(); e.printStackTrace() }
}

private fun shareImage(context: android.content.Context, path: String) {
    try {
        val file = File(path)
        if (!file.exists()) { Toast.makeText(context, "图片不存在", Toast.LENGTH_SHORT).show(); return }
        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "分享图片"))
    } catch (e: Exception) { Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show() }
}