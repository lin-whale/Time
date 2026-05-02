/**
 * 媒体附件选择器组件
 * 
 * 功能说明：
 * - 从相册选择图片
 * - 预览已选择的媒体
 * - 删除已选择的媒体
 * - 支持多图选择
 * 
 * 使用 ActivityResultLauncher 处理图片选择
 */
package com.example.time.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
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
 * 媒体附件选择器
 * 
 * @param mediaPaths 当前已选择的媒体路径列表
 * @param onMediaAdded 添加媒体回调（传入本地文件路径）
 * @param onMediaRemoved 移除媒体回调
 * @param maxMedia 最大媒体数量（默认9张）
 */
@Composable
fun MediaPicker(
    mediaPaths: List<String>,
    onMediaAdded: (String) -> Unit,
    onMediaRemoved: (String) -> Unit,
    maxMedia: Int = 9
) {
    val context = LocalContext.current
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            // 将URI复制到应用私有目录，确保持久访问
            val localPath = copyUriToLocal(context, uri)
            if (localPath != null) {
                onMediaAdded(localPath)
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
                items(mediaPaths) { path ->
                    MediaThumbnail(
                        path = path,
                        onRemove = { onMediaRemoved(path) }
                    )
                }
                
                // 添加按钮（如果还没达到最大数量）
                if (mediaPaths.size < maxMedia) {
                    item {
                        AddMediaButton(
                            onClick = { imagePickerLauncher.launch("image/*") }
                        )
                    }
                }
            }
        } else {
            // 没有媒体时显示添加按钮
            AddMediaButton(
                onClick = { imagePickerLauncher.launch("image/*") },
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
    path: String,
    onRemove: () -> Unit,
    onClick: (() -> Unit)? = null
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
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Gray.copy(alpha = 0.2f))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "媒体附件",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } ?: run {
            // 加载失败显示占位符
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("📷")
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
 * 添加媒体按钮
 */
@Composable
fun AddMediaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Gray.copy(alpha = 0.1f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加图片",
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "添加图片",
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 将URI复制到应用私有目录
 * 返回本地文件路径，失败返回null
 */
fun copyUriToLocal(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "media_${System.currentTimeMillis()}_${uri.lastPathSegment?.hashCode() ?: 0}"
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