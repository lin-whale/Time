/**
 * 简化版时间编辑方案
 * 
 * 设计理念：
 * 1. 默认"连续模式" - 修改边界时自动调整相邻记录，保持无缝连接
 * 2. 可选"独立模式" - 只修改当前记录，允许产生时间空隙
 * 3. 修改前预览影响，一键应用或取消
 */
package com.example.time.ui.timeRecord

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertTimeFormat
import kotlin.math.roundToLong

/**
 * 简化版时间编辑对话框
 * 
 * @param currentPiece 当前编辑的记录
 * @param prevPiece 前一条记录（时间上更早）
 * @param nextPiece 后一条记录（时间上更晚）
 * @param onSave 保存回调 (当前记录, 前一条调整, 后一条调整)
 * @param onDelete 删除回调
 * @param onCancel 取消回调
 */
@Composable
fun SimpleTimePieceEditDialog(
    currentPiece: TimePiece,
    prevPiece: TimePiece? = null,
    nextPiece: TimePiece? = null,
    onSave: (current: TimePiece, prev: TimePiece?, next: TimePiece?) -> Unit,
    onDelete: (TimePiece) -> Unit,
    onCancel: () -> Unit
) {
    // 编辑状态
    var editedFromTime by remember { mutableStateOf(currentPiece.fromTimePoint) }
    var editedToTime by remember { mutableStateOf(currentPiece.timePoint) }
    var editedMainEvent by remember { mutableStateOf(currentPiece.mainEvent) }
    var editedSubEvent by remember { mutableStateOf(currentPiece.subEvent) }
    var editedEmotion by remember { mutableStateOf(currentPiece.emotion) }
    var editedRecord by remember { mutableStateOf(currentPiece.lastTimeRecord) }
    
    // 模式：true=连续模式，false=独立模式
    var continuousMode by remember { mutableStateOf(true) }
    
    // 计算影响的记录
    val adjustedPrev = if (continuousMode && prevPiece != null && editedFromTime != currentPiece.fromTimePoint) {
        prevPiece.copy(timePoint = editedFromTime)
    } else null
    
    val adjustedNext = if (continuousMode && nextPiece != null && editedToTime != currentPiece.timePoint) {
        nextPiece.copy(fromTimePoint = editedToTime)
    } else null
    
    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 标题栏（固定在顶部）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✏️ 编辑时间",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = { onDelete(currentPiece) }) {
                        Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
                    }
                }
                
                Divider()
                
                // 可滚动内容区
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                
                // ===== 可视化时间轴 =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "时间范围",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 前一条记录（如果有）
                        if (prevPiece != null) {
                            TimelineBar(
                                label = "前一条: ${prevPiece.mainEvent}",
                                fromTime = prevPiece.fromTimePoint,
                                toTime = adjustedPrev?.timePoint ?: prevPiece.timePoint,
                                color = if (adjustedPrev != null) 
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                                else 
                                    Color.Gray.copy(alpha = 0.3f),
                                showChange = adjustedPrev != null
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // 当前编辑的记录
                        TimelineBar(
                            label = "当前: ${currentPiece.mainEvent}",
                            fromTime = editedFromTime,
                            toTime = editedToTime,
                            color = MaterialTheme.colorScheme.primary,
                            showChange = true,
                            editable = true
                        )
                        
                        // 后一条记录（如果有）
                        if (nextPiece != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TimelineBar(
                                label = "后一条: ${nextPiece.mainEvent}",
                                fromTime = adjustedNext?.fromTimePoint ?: nextPiece.fromTimePoint,
                                toTime = nextPiece.timePoint,
                                color = if (adjustedNext != null) 
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                                else 
                                    Color.Gray.copy(alpha = 0.3f),
                                showChange = adjustedNext != null
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ===== 时间调整控件 =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 开始时间
                    Column(modifier = Modifier.weight(1f)) {
                        Text("开始时间", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            convertTimeFormat(editedFromTime, "HH:mm"),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // 上下调整按钮
                        Row {
                            IconButton(onClick = { editedFromTime -= 5 * 60 * 1000 }) {
                                Text("-5", fontSize = 14.sp)
                            }
                            IconButton(onClick = { editedFromTime += 5 * 60 * 1000 }) {
                                Icon(Icons.Default.Add, "加5分钟", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    
                    Icon(Icons.Default.ArrowForward, null, modifier = Modifier.align(Alignment.CenterVertically))
                    
                    // 结束时间
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("结束时间", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            convertTimeFormat(editedToTime, "HH:mm"),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // 上下调整按钮
                        Row {
                            IconButton(onClick = { editedToTime -= 5 * 60 * 1000 }) {
                                Text("-5", fontSize = 14.sp)
                            }
                            IconButton(onClick = { editedToTime += 5 * 60 * 1000 }) {
                                Icon(Icons.Default.Add, "加5分钟", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ===== 模式切换 =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = continuousMode,
                        onCheckedChange = { continuousMode = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (continuousMode) "🔗 连续模式" else "✂️ 独立模式",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (continuousMode) "自动调整相邻记录，保持时间连续" else "只修改当前记录，可能产生时间空隙",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                
                // ===== 事件信息编辑 =====
                OutlinedTextField(
                    value = editedMainEvent,
                    onValueChange = { editedMainEvent = it },
                    label = { Text("主事件") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = editedSubEvent,
                    onValueChange = { editedSubEvent = it },
                    label = { Text("子事件") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 底部按钮区（固定）
            Column {
                Divider()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = {
                            val updated = currentPiece.copy(
                                fromTimePoint = editedFromTime,
                                timePoint = editedToTime,
                                mainEvent = editedMainEvent,
                                subEvent = editedSubEvent,
                                emotion = editedEmotion,
                                lastTimeRecord = editedRecord
                            )
                            onSave(updated, adjustedPrev, adjustedNext)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = editedMainEvent.isNotBlank() && editedFromTime < editedToTime
                    ) {
                        Text("💾 保存")
                    }
                }
            }
            }
        }
    }
}

/**
 * 时间轴条形图
 */
@Composable
fun TimelineBar(
    label: String,
    fromTime: Long,
    toTime: Long,
    color: Color,
    showChange: Boolean = false,
    editable: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (showChange) MaterialTheme.colorScheme.primary else Color.Gray
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(color, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${convertTimeFormat(fromTime, "HH:mm")} → ${convertTimeFormat(toTime, "HH:mm")}",
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = if (editable) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        if (showChange && !editable) {
            Text(
                text = "⚠️ 将被自动调整",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
