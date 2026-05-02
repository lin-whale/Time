/**
 * 媒体查看器组件
 * 
 * 功能说明：
 * - 全屏查看图片/GIF
 * - 播放视频
 * - 支持左右滑动切换
 * - 显示媒体序号
 */
package com.example.time.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.ViewGroup
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * 全屏媒体查看器
 */
@Composable
fun MediaViewer(
    mediaPaths: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableStateOf(initialIndex.coerceIn(0, (mediaPaths.size - 1).coerceAtLeast(0))) }
    
    // 解析媒体信息
    val mediaInfos = remember(mediaPaths) {
        mediaPaths.map { path -> parseMediaInfo(context, path) }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        when {
                            dragAmount > 50 && currentIndex > 0 -> currentIndex -= 1
                            dragAmount < -50 && currentIndex < mediaPaths.size - 1 -> currentIndex += 1
                        }
                    }
                }
        ) {
            // 顶部工具栏
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
                
                if (mediaPaths.isNotEmpty()) {
                    Text(
                        text = "${currentIndex + 1} / ${mediaPaths.size}",
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.size(48.dp))
            }
            
            // 媒体显示
            if (mediaPaths.isNotEmpty() && currentIndex in mediaInfos.indices) {
                val mediaInfo = mediaInfos[currentIndex]
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp, bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (mediaInfo.type) {
                        MediaType.IMAGE, MediaType.GIF -> {
                            ImageMediaViewer(mediaInfo = mediaInfo)
                        }
                        MediaType.VIDEO -> {
                            VideoMediaViewer(mediaInfo = mediaInfo)
                        }
                    }
                }
                
                // 底部指示器
                if (mediaPaths.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 30.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(mediaPaths.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (index == currentIndex) Color.White
                                        else Color.Gray.copy(alpha = 0.5f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 图片/GIF 查看器
 */
@Composable
fun ImageMediaViewer(mediaInfo: MediaInfo) {
    val context = LocalContext.current
    val bitmap = remember(mediaInfo.path) {
        try {
            if (mediaInfo.path.startsWith("/")) {
                BitmapFactory.decodeFile(mediaInfo.path)
            } else {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(mediaInfo.path))
                BitmapFactory.decodeStream(inputStream).also {
                    inputStream?.close()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "图片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            
            // GIF 标签
            if (mediaInfo.type == MediaType.GIF) {
                Text(
                    text = "GIF",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        } ?: Text(
            text = "📷 无法加载图片",
            color = Color.Gray,
            fontSize = 16.sp
        )
    }
}

/**
 * 视频播放器
 */
@Composable
fun VideoMediaViewer(mediaInfo: MediaInfo) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isError by remember { mutableStateOf(false) }
    
    // 创建播放器
    LaunchedEffect(mediaInfo.path) {
        try {
            val player = ExoPlayer.Builder(context).build()
            val mediaItem = if (mediaInfo.path.startsWith("/")) {
                MediaItem.fromUri(Uri.fromFile(java.io.File(mediaInfo.path)))
            } else {
                MediaItem.fromUri(Uri.parse(mediaInfo.path))
            }
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
            exoPlayer = player
        } catch (e: Exception) {
            isError = true
        }
    }
    
    // 释放播放器
    DisposableEffect(Unit) {
        onDispose {
            try {
                exoPlayer?.release()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isError) {
            Text(
                text = "🎬 无法播放视频",
                color = Color.Gray,
                fontSize = 16.sp
            )
        } else {
            exoPlayer?.let { player ->
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            try {
                                this.player = player
                                useController = true
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            } catch (e: Exception) {
                                // ignore
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // 视频时长显示
                if (mediaInfo.duration > 0) {
                    Text(
                        text = formatDuration(mediaInfo.duration),
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } ?: run {
                // 加载中
                Text(
                    text = "🎬 加载视频中...",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * 简单的媒体预览组件（显示在卡片中）
 */
@Composable
fun MediaPreviewRow(
    mediaPaths: List<String>,
    maxVisible: Int = 4,
    onMediaClick: (Int) -> Unit = {}
) {
    if (mediaPaths.isEmpty()) return
    
    val context = LocalContext.current
    val visibleCount = minOf(mediaPaths.size, maxVisible)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        mediaPaths.take(visibleCount).forEachIndexed { index, path ->
            val mediaInfo = remember(path) { parseMediaInfo(context, path) }
            SmallMediaThumbnail(
                mediaInfo = mediaInfo,
                onClick = { onMediaClick(index) }
            )
        }
        
        // 更多数量提示
        if (mediaPaths.size > maxVisible) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${mediaPaths.size - maxVisible}",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SmallMediaThumbnail(
    mediaInfo: MediaInfo,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Gray.copy(alpha = 0.2f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (mediaInfo.type) {
            MediaType.IMAGE, MediaType.GIF -> {
                val bitmap = remember(mediaInfo.path) {
                    try {
                        if (mediaInfo.path.startsWith("/")) {
                            BitmapFactory.decodeFile(mediaInfo.path)
                        } else {
                            val inputStream = context.contentResolver.openInputStream(Uri.parse(mediaInfo.path))
                     BitmapFactory.decodeStream(inputStream).also {
                                inputStream?.close()
                            }
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: Text("📷", fontSize = 16.sp)
                
                if (mediaInfo.type == MediaType.GIF) {
                    Text(
                        text = "GIF",
                        color = Color.White,
                        fontSize = 8.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(2.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
            MediaType.VIDEO -> {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "视频",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                
                if (mediaInfo.duration > 0) {
                    Text(
                        text = formatDuration(mediaInfo.duration),
                        color = Color.White,
                        fontSize = 8.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}