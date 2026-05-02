/**
 * 媒体查看器组件
 * 
 * 功能说明：
 * - 全屏查看图片
 * - 支持缩放和滑动切换
 * - 显示图片序号
 */
package com.example.time.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 全屏媒体查看器
 * 
 * @param mediaPaths 媒体路径列表
 * @param initialIndex 初始显示的索引
 * @param onDismiss 关闭回调
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaViewer(
    mediaPaths: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // 分页状态
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (mediaPaths.size - 1).coerceAtLeast(0)),
        pageCount = { mediaPaths.size }
    )
    
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
                // 返回按钮
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
                
                // 序号显示
                Text(
                    text = "${pagerState.currentPage + 1} / ${mediaPaths.size}",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                
                // 占位，保持两端对称
                Spacer(modifier = Modifier.size(48.dp))
            }
            
            // 图片滑动器
            if (mediaPaths.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp, bottom = 60.dp)
                ) { page ->
                    val path = mediaPaths[page]
                    val bitmap = remember(path) {
                        try {
                            if (path.startsWith("/")) {
                                BitmapFactory.decodeFile(path)
                            } else {
                                val inputStream = context.contentResolver.openInputStream(Uri.parse(path))
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
                                contentDescription = "媒体${page + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } ?: run {
                            Text(
                                text = "📷 无法加载图片",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
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
    
    val visibleCount = minOf(mediaPaths.size, maxVisible)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        mediaPaths.take(visibleCount).forEachIndexed { index, path ->
            SmallMediaThumbnail(
                path = path,
                onClick = { onMediaClick(index) }
            )
        }
        
        // 如果有更多图片，显示数量提示
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
    path: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(path) {
        try {
            if (path.startsWith("/")) {
                BitmapFactory.decodeFile(path)
            } else {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(path))
                BitmapFactory.decodeStream(inputStream).also {
                    inputStream?.close()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Gray.copy(alpha = 0.2f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } ?: Text("📷", fontSize = 16.sp)
    }
}