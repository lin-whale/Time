package com.example.time.ui.showTimePieces

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertTimeFormat
import com.example.time.ui.TimeViewModel
import com.example.time.ui.timeRecord.TimePickerDialog

/**
 * 在时间段中插入新记录的对话框
 * 功能：在现有TimePiece的时间范围内插入新的记录，自动切割原有记录
 * 
 * 插入逻辑：
 * 1. 用户选择插入的开始时间和结束时间
 * 2. 检查时间范围是否在原TimePiece的范围内
 * 3. 创建新的TimePiece
 * 4. 如果需要，切割原有TimePiece（更新其结束时间）
 * 5. 如果插入后还有剩余时间段，创建一个新的TimePiece填充剩余时间
 */
@Composable
fun TimePieceInsertDialog(
    originalTimePiece: TimePiece,
    viewModel: TimeViewModel,
    onDismiss: () -> Unit
) {
    // 新记录的状态变量
    var newMainEvent by remember { mutableStateOf("") }
    var newSubEvent by remember { mutableStateOf("") }
    var newLastTimeRecord by remember { mutableStateOf("") }
    var newEmotion by remember { mutableIntStateOf(3) }
    
    // 插入时间范围
    var insertStartTime by remember { mutableLongStateOf(originalTimePiece.fromTimePoint) }
    var insertEndTime by remember { mutableLongStateOf(originalTimePiece.timePoint) }
    
    // 时间选择器状态
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "在时间段中插入新记录",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "原时间段：${convertTimeFormat(originalTimePiece.fromTimePoint, "M/d HH:mm")} - ${convertTimeFormat(originalTimePiece.timePoint, "M/d HH:mm")}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                // 插入开始时间
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("插入开始：")
                    Button(onClick = { showStartTimePicker = true }) {
                        Text(convertTimeFormat(insertStartTime, "M/d HH:mm"))
                    }
                }

                // 插入结束时间
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("插入结束：")
                    Button(onClick = { showEndTimePicker = true }) {
                        Text(convertTimeFormat(insertEndTime, "M/d HH:mm"))
                    }
                }

                // 主事件
                TextField(
                    value = newMainEvent,
                    onValueChange = { newMainEvent = it },
                    label = { Text("主事件") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 子事件
                TextField(
                    value = newSubEvent,
                    onValueChange = { newSubEvent = it },
                    label = { Text("子事件") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 体验记录
                TextField(
                    value = newLastTimeRecord,
                    onValueChange = { newLastTimeRecord = it },
                    label = { Text("体验记录") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // 情感评分
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("情感：")
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (index < newEmotion) Color.Yellow else Color.Gray,
                                modifier = Modifier
                                    .clickable {
                                        newEmotion = index + 1
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
                
                // 错误信息
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onDismiss) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            // 验证时间范围
                            if (insertStartTime < originalTimePiece.fromTimePoint || 
                                insertEndTime > originalTimePiece.timePoint) {
                                errorMessage = "插入时间必须在原时间段范围内"
                                return@Button
                            }
                            
                            if (insertStartTime >= insertEndTime) {
                                errorMessage = "开始时间必须早于结束时间"
                                return@Button
                            }
                            
                            // 检查是否完全替换原记录（不是真正的插入）
                            // 插入功能用于在时间段内添加新记录，如果完全覆盖原时间段，
                            // 应该使用编辑功能而不是插入功能
                            if (insertStartTime == originalTimePiece.fromTimePoint && 
                                insertEndTime == originalTimePiece.timePoint) {
                                errorMessage = "插入时间不能完全覆盖原时间段，请使用编辑功能"
                                return@Button
                            }
                            
                            if (newMainEvent.isEmpty()) {
                                errorMessage = "主事件不能为空"
                                return@Button
                            }
                            
                            // 执行插入和切割操作
                            performInsertAndSplit(
                                original = originalTimePiece,
                                insertStart = insertStartTime,
                                insertEnd = insertEndTime,
                                newMainEvent = newMainEvent,
                                newSubEvent = newSubEvent,
                                newLastTimeRecord = newLastTimeRecord,
                                newEmotion = newEmotion,
                                viewModel = viewModel
                            )
                            
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF80CBC4))
                    ) {
                        Text("插入并切割")
                    }
                }
            }
        }
    }

    // 开始时间选择器
    if (showStartTimePicker) {
        TimePickerDialog(
            latestTime = insertStartTime,
            onTimeSelected = {
                insertStartTime = it
                showStartTimePicker = false
            },
            onCancel = {
                showStartTimePicker = false
            }
        )
    }

    // 结束时间选择器
    if (showEndTimePicker) {
        TimePickerDialog(
            latestTime = insertEndTime,
            onTimeSelected = {
                insertEndTime = it
                showEndTimePicker = false
            },
            onCancel = {
                showEndTimePicker = false
            }
        )
    }
}

/**
 * 执行插入和切割操作
 * 
 * 场景说明：
 * 原记录: [fromTimePoint -------- timePoint]
 * 
 * 情况1：插入在开始
 * [fromTimePoint -- insertEnd] [新记录] [insertEnd -------- timePoint] (保留原记录后半段)
 * 
 * 情况2：插入在结束
 * [fromTimePoint -------- insertStart] [新记录] [insertStart -- timePoint]
 * 
 * 情况3：插入在中间
 * [fromTimePoint -- insertStart] [新记录] [insertStart--insertEnd] [剩余时间段] [insertEnd -- timePoint]
 */
private fun performInsertAndSplit(
    original: TimePiece,
    insertStart: Long,
    insertEnd: Long,
    newMainEvent: String,
    newSubEvent: String,
    newLastTimeRecord: String,
    newEmotion: Int,
    viewModel: TimeViewModel
) {
    // 1. 创建新插入的TimePiece
    val newTimePiece = TimePiece(
        timePoint = insertEnd,
        fromTimePoint = insertStart,
        emotion = newEmotion,
        lastTimeRecord = newLastTimeRecord,
        mainEvent = newMainEvent,
        subEvent = newSubEvent
    )
    viewModel.insertTimePiece(newTimePiece)
    
    // 2. 如果插入点不在开始位置，需要创建前半段记录
    if (insertStart > original.fromTimePoint) {
        val beforePiece = TimePiece(
            timePoint = insertStart,
            fromTimePoint = original.fromTimePoint,
            emotion = original.emotion,
            lastTimeRecord = original.lastTimeRecord,
            mainEvent = original.mainEvent,
            subEvent = original.subEvent
        )
        viewModel.insertTimePiece(beforePiece)
    }
    
    // 3. 如果插入点不在结束位置，需要创建后半段记录
    if (insertEnd < original.timePoint) {
        val afterPiece = TimePiece(
            timePoint = original.timePoint,
            fromTimePoint = insertEnd,
            emotion = original.emotion,
            lastTimeRecord = original.lastTimeRecord,
            mainEvent = original.mainEvent,
            subEvent = original.subEvent
        )
        viewModel.insertTimePiece(afterPiece)
    }
    
    // 4. 删除原始记录
    viewModel.deleteTimePiece(original)
}
