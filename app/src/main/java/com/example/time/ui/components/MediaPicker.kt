/**
 * 媒体附件选择器组件
 * 
 * 功能说明：
 * - 从相册选择图片/视频
 * - 预览已选择的媒体
 * - 删除已选择的媒体
 * - 支持多选，最多9个
 */
package com.example.time.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream

/**
 * 媒体类型枚举
 */
enum class MediaType {
    IMAGE, VIDEO, GIF
}

/**
 * 媒体信息
 */
data class MediaInfo(
    val path: String,
    val type: MediaType,
    val duration: Long = 0  // 视频时长（毫秒）
)

/**
 * 媒体附件选择器
 */
@Composable
fun MediaPicker(
    mediaPaths: List<String>,
    onMediaAdded: (String) -> Unit,
    onMediaRemoved: (String) -> Unit,
    maxMedia: Int = 9
) {
    val context = LocalContext.current
    
    // 解析媒体信息
    val mediaInfos = remember(mediaPaths) {
        mediaPaths.map { path -> parseMediaInfo(context, path) }
    }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            if (mediaPaths.size < maxMedia) {
                val localPath = copyUriToLocal(context, uri)
                if (localPath != null) {
                    onMediaAdded(localPath)
                }
            }
        }
    }
    
    // 视频选择器
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (mediaPaths.size < maxMedia) {
                val localPath = copyUriToLocal(context, it)
                if (localPath != null) {
                    onMediaAdded(localPath)
                }
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 已选择的媒体列表
        if (mediaPaths.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(mediaInfos) { mediaInfo ->
                    MediaThumbnail(
                        mediaInfo = mediaInfo,
                        onRemove = { onMediaRemoved(mediaInfo.path) }
                    )
                }
                
                // 添加按钮
                if (mediaPaths.size < maxMedia) {
                    item {
                        AddMediaButton(
                            onImageClick = { imagePickerLauncher.launch("image/*") },
                            onVideoClick = { videoPickerLauncher.launch("video/*") }
                        )
                    }
                }
            }
        } else {
            // 没有媒体时显示添加按钮
            AddMediaButton(
                onImageClick = { imagePickerLauncher.launch("image/*") },
                onVideoClick = { videoPickerLauncher.launch("video/*") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 媒体缩略图
 */
@Composable
fun MediaThumbnail(
    mediaInfo: MediaInfo,
    onRemove: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Gray.copy(alpha = 0.2f))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
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
                        contentDescription = "图片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷")
                }
                
                // GIF 标签
                if (mediaInfo.type == MediaType.GIF) {
                    Text(
                        text = "GIF",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            MediaType.VIDEO -> {
                // 视频缩略图
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎬", fontSize = 24.sp)
                    
                    // 播放图标
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "视频",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center)
                    )
                    
                    // 时长显示
                    if (mediaInfo.duration > 0) {
                        Text(
                            text = formatDuration(mediaInfo.duration),
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
        
        // 删除按钮
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "删除",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * 添加媒体按钮（支持图片和视频）
 */
@Composable
fun AddMediaButton(
    onImageClick: () -> Unit,
    onVideoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 添加图片按钮
        Box(
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray.copy(alpha = 0.1f))
                .clickable { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加图片",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "图片",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
        
        // 添加视频按钮
        Box(
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray.copy(alpha = 0.1f))
                .clickable { onVideoClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎬", fontSize = 24.sp)
                Text(
                    text = "视频",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * 解析媒体信息
 */
fun parseMediaInfo(context: Context, path: String): MediaInfo {
    return try {
        val extension = path.substringAfterLast(".").lowercase()
        val fileName = path.substringAfterLast("/")
        
        when {
            extension == "gif" -> MediaInfo(path, MediaType.GIF)
            extension in listOf("mp4", "3gp", "webm", "mkv", "mov") -> {
                // 获取视频时长
                val duration = getVideoDuration(context, path)
                MediaInfo(path, MediaType.VIDEO, duration)
            }
            // 尝试从文件名判断是否为实况图（iOS Live Photo）
            fileName.contains("live", ignoreCase = true) || 
            fileName.contains("motion", ignoreCase = true) -> {
                MediaInfo(path, MediaType.VIDEO, getVideoDuration(context, path))
            }
            else -> MediaInfo(path, MediaType.IMAGE)
        }
    } catch (e: Exception) {
        // 发生错误时默认当作图片处理
        MediaInfo(path, MediaType.IMAGE)
    }
}

/**
 * 获取视频时长
 */
fun getVideoDuration(context: Context, path: String): Long {
    return try {
        val retriever = android.media.MediaMetadataRetriever()
        if (path.startsWith("/")) {
            retriever.setDataSource(path)
        } else {
            retriever.setDataSource(context, Uri.parse(path))
        }
        val duration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
        retriever.release()
        duration
    } catch (e: Exception) {
        0L
    }
}

/**
 * 格式化时长
 */
fun formatDuration(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / 1000) / 60
    return if (minutes > 0) {
        "${minutes}:${seconds.toString().padStart(2, '0')}"
    } else {
        "0:${seconds.toString().padStart(2, '0')}"
    }
}

/**
 * 将URI复制到应用私有目录
 */
fun copyUriToLocal(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        
        // 获取文件名和扩展名
        val fileName = try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "media_${System.currentTimeMillis()}"
        } catch (e: Exception) {
            "media_${System.currentTimeMillis()}"
        }
        
        val file = File(context.filesDir, "media")
        if (!file.exists()) file.mkdirs()
        val destFile = File(file, fileName)
        
        FileOutputStream(destFile).use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        destFile.absolutePath
    } catch (e: Exception) {
        null
    }
}