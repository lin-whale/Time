/**
 * 时间片段编辑对话框
 * 
 * 功能说明：
 * - 编辑已有TimePiece的开始时间、结束时间
 * - 编辑事件名称（mainEvent:subEvent）
 * - 编辑情绪评分和体验记录
 * - 支持删除时间片段
 * - 支持在中间插入新的时间片段（自动切割）
 * 
 * 开发原则：
 * - 所有数据仅在本地处理，不上传网络
 * - 修改后自动更新相关统计数据
 */
package com.example.time.ui.timeRecord

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertTimeFormat
import com.example.time.ui.theme.EmotionColors

/**
 * 时间片段编辑对话框
 * 
 * @param timePiece 要编辑的时间片段
 * @param onSave 保存回调，返回修改后的TimePiece
 * @param onDelete 删除回调
 * @param onInsertBefore 在此记录前插入新片段的回调（用于时间切割）
 * @param onCancel 取消回调
 * @param minTime 最小可选时间（前一条记录的结束时间）
 * @param maxTime 最大可选时间（后一条记录的开始时间，如果是最新记录则为当前时间）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePieceEditDialog(
    timePiece: TimePiece,
    onSave: (TimePiece) -> Unit,
    onDelete: (TimePiece) -> Unit,
    onInsertBefore: ((newPieceEndTime: Long, originalPiece: TimePiece) -> Unit)? = null,
    onCancel: () -> Unit,
    minTime: Long = 0L,
    maxTime: Long = System.currentTimeMillis()
) {
    // 编辑状态
    var editedMainEvent by remember { mutableStateOf(timePiece.mainEvent) }
    var editedSubEvent by remember { mutableStateOf(timePiece.subEvent) }
    var editedEmotion by remember { mutableStateOf(timePiece.emotion) }
    var editedRecord by remember { mutableStateOf(timePiece.lastTimeRecord) }
    var editedFromTime by remember { mutableStateOf(timePiece.fromTimePoint) }
    var editedToTime by remember { mutableStateOf(timePiece.timePoint) }
    
    // 时间选择器状态
    var isEditingFromTime by remember { mutableStateOf(false) }
    var isEditingToTime by remember { mutableStateOf(false) }
    
    // 删除确认对话框
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    // 插入新片段对话框
    var showInsertDialog by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✏️ 编辑记录",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // 删除按钮
                    IconButton(
                        onClick = { showDeleteConfirm = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))
                
                // ===== 时间编辑区域 =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "⏱️ 时间段",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 开始时间
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isEditingFromTime = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "开始时间",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = convertTimeFormat(editedFromTime, "MM/dd HH:mm"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 结束时间
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isEditingToTime = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "结束时间",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = convertTimeFormat(editedToTime, "MM/dd HH:mm"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // 时长显示
                        Spacer(modifier = Modifier.height(8.dp))
                        val durationMinutes = (editedToTime - editedFromTime) / 60000
                        val hours = durationMinutes / 60
                        val minutes = durationMinutes % 60
                        Text(
                            text = "时长：${hours}小时${minutes}分钟",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // ===== 事件编辑区域 =====
                OutlinedTextField(
                    value = editedMainEvent,
                    onValueChange = { editedMainEvent = it },
                    label = { Text("主事件") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = editedSubEvent,
                    onValueChange = { editedSubEvent = it },
                    label = { Text("子事件（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // ===== 情绪评分 =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "心情：",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < editedEmotion) 
                                EmotionColors.getColorForStar(editedEmotion) 
                            else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { editedEmotion = index + 1 }
                                .padding(2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // ===== 体验记录 =====
                OutlinedTextField(
                    value = editedRecord,
                    onValueChange = { editedRecord = it },
                    label = { Text("体验记录（可选）") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ===== 插入新片段按钮 =====
                if (onInsertBefore != null) {
                    OutlinedButton(
                        onClick = { showInsertDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("✂️ 在此时间段中插入新记录")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // ===== 操作按钮 =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = {
                            // 构建修改后的TimePiece
                            val updatedPiece = timePiece.copy(
                                mainEvent = editedMainEvent,
                                subEvent = editedSubEvent,
                                emotion = editedEmotion,
                                lastTimeRecord = editedRecord,
                                fromTimePoint = editedFromTime,
                                timePoint = editedToTime
                            )
                            onSave(updatedPiece)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        enabled = editedMainEvent.isNotBlank()
                    ) {
                        Text("💾 保存")
                    }
                }
            }
        }
    }
    
    // 开始时间选择器 - 范围：前一条开始时间 到 当前结束时间
    if (isEditingFromTime) {
        TimePickerDialog(
            latestTime = if (minTime > 0) minTime - 24 * 60 * 60 * 1000L else 0L,  // 允许选择更早的时间
            maxTime = editedToTime,
            onTimeSelected = { newTime ->
                if (newTime < editedToTime) {
                    editedFromTime = newTime
                }
                isEditingFromTime = false
            },
            onCancel = { isEditingFromTime = false }
        )
    }
    
    // 结束时间选择器 - 范围：当前开始时间 到 后一条结束时间或当前时间+1天
    if (isEditingToTime) {
        TimePickerDialog(
            latestTime = editedFromTime,
            maxTime = if (maxTime < System.currentTimeMillis()) maxTime + 24 * 60 * 60 * 1000L else System.currentTimeMillis(),
            onTimeSelected = { newTime ->
                if (newTime > editedFromTime) {
                    editedToTime = newTime
                }
                isEditingToTime = false
            },
            onCancel = { isEditingToTime = false }
        )
    }
    
    // 删除确认对话框
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(timePiece)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 插入新片段对话框
    if (showInsertDialog && onInsertBefore != null) {
        InsertTimePieceDialog(
            originalPiece = timePiece,
            onInsert = { splitTime ->
                onInsertBefore(splitTime, timePiece)
                showInsertDialog = false
            },
            onCancel = { showInsertDialog = false }
        )
    }
}

/**
 * 插入新时间片段对话框
 * 用于在现有时间片段中间插入新记录，自动切割时间
 */
@Composable
fun InsertTimePieceDialog(
    originalPiece: TimePiece,
    onInsert: (splitTime: Long) -> Unit,
    onCancel: () -> Unit
) {
    // 默认选择中间时间点
    var splitTime by remember { 
        mutableStateOf((originalPiece.fromTimePoint + originalPiece.timePoint) / 2) 
    }
    var isSelectingTime by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✂️ 插入新记录",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "选择切割时间点：",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 显示时间范围
                Text(
                    text = "${convertTimeFormat(originalPiece.fromTimePoint, "MM/dd HH:mm")} → ${convertTimeFormat(originalPiece.timePoint, "MM/dd HH:mm")}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 切割点选择
                OutlinedButton(
                    onClick = { isSelectingTime = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("切割点：${convertTimeFormat(splitTime, "MM/dd HH:mm")}")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "新记录将占用：${convertTimeFormat(originalPiece.fromTimePoint, "HH:mm")} → ${convertTimeFormat(splitTime, "HH:mm")}\n" +
                           "原记录将变为：${convertTimeFormat(splitTime, "HH:mm")} → ${convertTimeFormat(originalPiece.timePoint, "HH:mm")}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = { onInsert(splitTime) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("确认插入")
                    }
                }
            }
        }
    }
    
    if (isSelectingTime) {
        // 切割点必须在当前记录的时间范围内
        TimePickerDialog(
            latestTime = originalPiece.fromTimePoint,  // 最小时间：记录开始时间
            maxTime = originalPiece.timePoint,          // 最大时间：记录结束时间
            onTimeSelected = { newTime ->
                // 确保切割点在原时间段范围内
                if (newTime > originalPiece.fromTimePoint && newTime < originalPiece.timePoint) {
                    splitTime = newTime
                }
                isSelectingTime = false
            },
            onCancel = { isSelectingTime = false }
        )
    }
}
