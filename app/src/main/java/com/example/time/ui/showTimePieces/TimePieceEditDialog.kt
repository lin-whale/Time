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
 * TimePiece编辑对话框
 * 功能：
 * 1. 编辑开始和结束时间
 * 2. 编辑主事件和子事件
 * 3. 编辑体验记录
 * 4. 编辑情感评分
 * 5. 删除记录
 */
@Composable
fun TimePieceEditDialog(
    timePiece: TimePiece,
    viewModel: TimeViewModel,
    onDismiss: () -> Unit
) {
    // 可编辑的状态变量
    var editedMainEvent by remember { mutableStateOf(timePiece.mainEvent) }
    var editedSubEvent by remember { mutableStateOf(timePiece.subEvent) }
    var editedLastTimeRecord by remember { mutableStateOf(timePiece.lastTimeRecord) }
    var editedEmotion by remember { mutableIntStateOf(timePiece.emotion) }
    var editedFromTime by remember { mutableLongStateOf(timePiece.fromTimePoint) }
    var editedToTime by remember { mutableLongStateOf(timePiece.timePoint) }
    
    // 时间选择器状态
    var showFromTimePicker by remember { mutableStateOf(false) }
    var showToTimePicker by remember { mutableStateOf(false) }
    
    // 插入对话框状态
    var showInsertDialog by remember { mutableStateOf(false) }
    
    // 错误信息状态
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
                    text = "编辑时间记录",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 开始时间编辑
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("开始时间：")
                    Button(onClick = { showFromTimePicker = true }) {
                        Text(convertTimeFormat(editedFromTime, "M/d HH:mm"))
                    }
                }

                // 结束时间编辑
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("结束时间：")
                    Button(onClick = { showToTimePicker = true }) {
                        Text(convertTimeFormat(editedToTime, "M/d HH:mm"))
                    }
                }

                // 主事件编辑
                TextField(
                    value = editedMainEvent,
                    onValueChange = { editedMainEvent = it },
                    label = { Text("主事件") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 子事件编辑
                TextField(
                    value = editedSubEvent,
                    onValueChange = { editedSubEvent = it },
                    label = { Text("子事件") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 体验记录编辑
                TextField(
                    value = editedLastTimeRecord,
                    onValueChange = { editedLastTimeRecord = it },
                    label = { Text("体验记录") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // 情感评分编辑
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
                                tint = if (index < editedEmotion) Color(0xFFFFD600) else Color.Gray,
                                modifier = Modifier
                                    .clickable {
                                        editedEmotion = index + 1
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
                
                // 错误信息显示
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            // 打开插入对话框
                            showInsertDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF80CBC4))
                    ) {
                        Text("插入记录", color = Color.White)
                    }
                    
                    Button(
                        onClick = {
                            // 删除记录
                            viewModel.deleteTimePiece(timePiece)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("删除", color = Color.White)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onDismiss) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            // 验证输入
                            if (editedMainEvent.isEmpty()) {
                                errorMessage = "主事件不能为空"
                                return@Button
                            }
                            
                            if (editedFromTime >= editedToTime) {
                                errorMessage = "开始时间必须早于结束时间"
                                return@Button
                            }
                            
                            // 保存更新
                            val updatedTimePiece = timePiece.copy(
                                mainEvent = editedMainEvent,
                                subEvent = editedSubEvent,
                                lastTimeRecord = editedLastTimeRecord,
                                emotion = editedEmotion,
                                fromTimePoint = editedFromTime,
                                timePoint = editedToTime
                            )
                            viewModel.updateTimePiece(updatedTimePiece)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBFE9FF))
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }

    // 开始时间选择器
    if (showFromTimePicker) {
        TimePickerDialog(
            latestTime = editedFromTime,
            onTimeSelected = {
                editedFromTime = it
                showFromTimePicker = false
            },
            onCancel = {
                showFromTimePicker = false
            }
        )
    }

    // 结束时间选择器
    if (showToTimePicker) {
        TimePickerDialog(
            latestTime = editedToTime,
            onTimeSelected = {
                editedToTime = it
                showToTimePicker = false
            },
            onCancel = {
                showToTimePicker = false
            }
        )
    }
    
    // 显示插入对话框
    if (showInsertDialog) {
        TimePieceInsertDialog(
            originalTimePiece = timePiece,
            viewModel = viewModel,
            onDismiss = { 
                showInsertDialog = false
                onDismiss() // 插入完成后也关闭编辑对话框
            }
        )
    }
}
