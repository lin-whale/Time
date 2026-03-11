/**
 * TimePieceList - 时间片段列表组件
 * 支持点击编辑功能，包含时间一致性检查
 */
package com.example.time.ui.showTimePieces

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertDurationFormat
import com.example.time.logic.utils.convertTimeFormat
import com.example.time.ui.TimeViewModel
import com.example.time.ui.timeRecord.TimePieceEditDialog

/**
 * 时间冲突类型
 */
sealed class TimeConflict {
    /** 与前一条记录有间隙 */
    data class GapWithPrevious(
        val previousPiece: TimePiece,
        val currentPiece: TimePiece,
        val gapStart: Long,
        val gapEnd: Long
    ) : TimeConflict()
    
    /** 与后一条记录有间隙 */
    data class GapWithNext(
        val currentPiece: TimePiece,
        val nextPiece: TimePiece,
        val gapStart: Long,
        val gapEnd: Long
    ) : TimeConflict()
    
    /** 与前一条记录重叠 */
    data class OverlapWithPrevious(
        val previousPiece: TimePiece,
        val currentPiece: TimePiece,
        val overlapStart: Long,
        val overlapEnd: Long
    ) : TimeConflict()
    
    /** 与后一条记录重叠 */
    data class OverlapWithNext(
        val currentPiece: TimePiece,
        val nextPiece: TimePiece,
        val overlapStart: Long,
        val overlapEnd: Long
    ) : TimeConflict()
}

/**
 * 时间片段列表（Column布局）
 */
@Composable
fun TimePieceListColumn(timePieces: List<TimePiece>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (timePiece in timePieces) {
            TimePieceCard(timePiece = timePiece)
        }
    }
}

/**
 * 时间片段列表
 * @param timePieces 时间片段列表（按时间倒序）
 * @param viewModel 传入后支持点击编辑
 */
@Composable
fun TimePieceList(
    timePieces: List<TimePiece>,
    viewModel: TimeViewModel? = null
) {
    // 当前正在编辑的记录
    var editingPiece by remember { mutableStateOf<TimePiece?>(null) }
    
    // 时间冲突状态
    var timeConflict by remember { mutableStateOf<TimeConflict?>(null) }
    var pendingUpdate by remember { mutableStateOf<TimePiece?>(null) }
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(timePieces) { timePiece ->
            TimePieceCard(
                timePiece = timePiece,
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = if (viewModel != null) {
                    { editingPiece = timePiece }
                } else null
            )
        }
    }
    
    // 显示编辑对话框
    if (viewModel != null && editingPiece != null) {
        val piece = editingPiece!!
        val pieceIndex = timePieces.indexOf(piece)
        
        // 获取相邻记录（注意：列表是倒序的，index小的是更新的记录）
        val nextPiece = if (pieceIndex > 0) timePieces[pieceIndex - 1] else null  // 时间上的下一条
        val prevPiece = if (pieceIndex < timePieces.size - 1) timePieces[pieceIndex + 1] else null  // 时间上的上一条
        
        // 计算时间范围限制（宽松限制，允许用户调整后再处理冲突）
        val minTime = prevPiece?.fromTimePoint ?: 0L
        val maxTime = nextPiece?.timePoint ?: System.currentTimeMillis()
        
        TimePieceEditDialog(
            timePiece = piece,
            onSave = { updatedPiece ->
                // 检查时间一致性
                val conflict = checkTimeConflict(updatedPiece, prevPiece, nextPiece)
                if (conflict != null) {
                    // 有冲突，显示处理对话框
                    timeConflict = conflict
                    pendingUpdate = updatedPiece
                } else {
                    // 无冲突，直接保存
                    viewModel.updateTimePiece(updatedPiece)
                    editingPiece = null
                }
            },
            onDelete = { deletedPiece ->
                viewModel.deleteTimePiece(deletedPiece)
                editingPiece = null
            },
            onInsertBefore = { splitTime, originalPiece ->
                val newPiece = TimePiece(
                    timePoint = splitTime,
                    fromTimePoint = originalPiece.fromTimePoint,
                    emotion = originalPiece.emotion,
                    lastTimeRecord = "",
                    mainEvent = originalPiece.mainEvent,
                    subEvent = originalPiece.subEvent
                )
                viewModel.insertTimePieceWithSplit(splitTime, originalPiece, newPiece)
                editingPiece = null
            },
            onCancel = { editingPiece = null },
            minTime = minTime,
            maxTime = maxTime
        )
    }
    
    // 时间冲突处理对话框
    timeConflict?.let { conflict ->
        TimeConflictDialog(
            conflict = conflict,
            onResolve = { resolution ->
                pendingUpdate?.let { updated ->
                    when (resolution) {
                        is ConflictResolution.AdjustCurrent -> {
                            viewModel?.updateTimePiece(resolution.adjustedPiece)
                        }
                        is ConflictResolution.AdjustNeighbor -> {
                            viewModel?.updateTimePiece(updated)
                            viewModel?.updateTimePiece(resolution.adjustedNeighbor)
                        }
                        is ConflictResolution.AdjustBoth -> {
                            viewModel?.updateTimePiece(resolution.adjustedCurrent)
                            viewModel?.updateTimePiece(resolution.adjustedNeighbor)
                        }
                    }
                }
                timeConflict = null
                pendingUpdate = null
                editingPiece = null
            },
            onCancel = {
                timeConflict = null
                pendingUpdate = null
                // 不关闭编辑对话框，让用户重新编辑
            }
        )
    }
}

/**
 * 检查时间冲突
 */
private fun checkTimeConflict(
    current: TimePiece,
    prevPiece: TimePiece?,  // 时间上的前一条（开始时间更早）
    nextPiece: TimePiece?   // 时间上的后一条（开始时间更晚）
): TimeConflict? {
    // 检查与前一条的关系
    prevPiece?.let { prev ->
        when {
            // 重叠：当前开始时间 < 前一条结束时间
            current.fromTimePoint < prev.timePoint -> {
                return TimeConflict.OverlapWithPrevious(
                    previousPiece = prev,
                    currentPiece = current,
                    overlapStart = current.fromTimePoint,
                    overlapEnd = prev.timePoint
                )
            }
            // 间隙：当前开始时间 > 前一条结束时间
            current.fromTimePoint > prev.timePoint -> {
                return TimeConflict.GapWithPrevious(
                    previousPiece = prev,
                    currentPiece = current,
                    gapStart = prev.timePoint,
                    gapEnd = current.fromTimePoint
                )
            }
        }
    }
    
    // 检查与后一条的关系
    nextPiece?.let { next ->
        when {
            // 重叠：当前结束时间 > 后一条开始时间
            current.timePoint > next.fromTimePoint -> {
                return TimeConflict.OverlapWithNext(
                    currentPiece = current,
                    nextPiece = next,
                    overlapStart = next.fromTimePoint,
                    overlapEnd = current.timePoint
                )
            }
            // 间隙：当前结束时间 < 后一条开始时间
            current.timePoint < next.fromTimePoint -> {
                return TimeConflict.GapWithNext(
                    currentPiece = current,
                    nextPiece = next,
                    gapStart = current.timePoint,
                    gapEnd = next.fromTimePoint
                )
            }
        }
    }
    
    return null
}

/**
 * 冲突解决方案
 */
sealed class ConflictResolution {
    data class AdjustCurrent(val adjustedPiece: TimePiece) : ConflictResolution()
    data class AdjustNeighbor(val adjustedNeighbor: TimePiece) : ConflictResolution()
    data class AdjustBoth(val adjustedCurrent: TimePiece, val adjustedNeighbor: TimePiece) : ConflictResolution()
}

/**
 * 时间冲突处理对话框
 */
@Composable
fun TimeConflictDialog(
    conflict: TimeConflict,
    onResolve: (ConflictResolution) -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = when (conflict) {
                    is TimeConflict.GapWithPrevious, is TimeConflict.GapWithNext -> "⚠️ 检测到时间间隙"
                    is TimeConflict.OverlapWithPrevious, is TimeConflict.OverlapWithNext -> "⚠️ 检测到时间重叠"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                when (conflict) {
                    is TimeConflict.GapWithPrevious -> {
                        Text("前一条记录与当前记录之间有时间间隙：")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${convertTimeFormat(conflict.gapStart, "MM/dd HH:mm")} → ${convertTimeFormat(conflict.gapEnd, "MM/dd HH:mm")}",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "间隙时长：${convertDurationFormat(conflict.gapEnd - conflict.gapStart, "%d时%d分")}",
                            fontSize = 14.sp
                        )
                    }
                    is TimeConflict.GapWithNext -> {
                        Text("当前记录与后一条记录之间有时间间隙：")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${convertTimeFormat(conflict.gapStart, "MM/dd HH:mm")} → ${convertTimeFormat(conflict.gapEnd, "MM/dd HH:mm")}",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "间隙时长：${convertDurationFormat(conflict.gapEnd - conflict.gapStart, "%d时%d分")}",
                            fontSize = 14.sp
                        )
                    }
                    is TimeConflict.OverlapWithPrevious -> {
                        Text("当前记录与前一条记录时间重叠：")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${convertTimeFormat(conflict.overlapStart, "MM/dd HH:mm")} → ${convertTimeFormat(conflict.overlapEnd, "MM/dd HH:mm")}",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "重叠时长：${convertDurationFormat(conflict.overlapEnd - conflict.overlapStart, "%d时%d分")}",
                            fontSize = 14.sp
                        )
                    }
                    is TimeConflict.OverlapWithNext -> {
                        Text("当前记录与后一条记录时间重叠：")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${convertTimeFormat(conflict.overlapStart, "MM/dd HH:mm")} → ${convertTimeFormat(conflict.overlapEnd, "MM/dd HH:mm")}",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "重叠时长：${convertDurationFormat(conflict.overlapEnd - conflict.overlapStart, "%d时%d分")}",
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("请选择处理方式：", fontWeight = FontWeight.Medium)
            }
        },
        confirmButton = {
            Column {
                when (conflict) {
                    is TimeConflict.GapWithPrevious -> {
                        // 选项1：延长当前记录的开始时间
                        Button(
                            onClick = {
                                val adjusted = conflict.currentPiece.copy(
                                    fromTimePoint = conflict.previousPiece.timePoint
                                )
                                onResolve(ConflictResolution.AdjustCurrent(adjusted))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("延长当前记录（填补间隙）")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // 选项2：延长前一条记录的结束时间
                        OutlinedButton(
                            onClick = {
                                val adjusted = conflict.previousPiece.copy(
                                    timePoint = conflict.currentPiece.fromTimePoint
                                )
                                onResolve(ConflictResolution.AdjustNeighbor(adjusted))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("延长前一条记录")
                        }
                    }
                    is TimeConflict.GapWithNext -> {
                        // 选项1：延长当前记录的结束时间
                        Button(
                            onClick = {
                                val adjusted = conflict.currentPiece.copy(
                                    timePoint = conflict.nextPiece.fromTimePoint
                                )
                                onResolve(ConflictResolution.AdjustCurrent(adjusted))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("延长当前记录（填补间隙）")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // 选项2：延长后一条记录的开始时间
                        OutlinedButton(
                            onClick = {
                                val adjusted = conflict.nextPiece.copy(
                                    fromTimePoint = conflict.currentPiece.timePoint
                                )
                                onResolve(ConflictResolution.AdjustNeighbor(adjusted))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("延长后一条记录")
                        }
                    }
                    is TimeConflict.OverlapWithPrevious -> {
                        // 选项1：缩短当前记录（推迟开始时间）
                        Button(
                            onClick = {
                                val adjusted = conflict.currentPiece.copy(
                                    fromTimePoint = conflict.previousPiece.timePoint
                                )
                                onResolve(ConflictResolution.AdjustCurrent(adjusted))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("缩短当前记录（消除重叠）")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // 选项2：缩短前一条记录
                        OutlinedButton(
                            onClick = {
                                val adjusted = conflict.previousPiece.copy(
                                    timePoint = conflict.currentPiece.fromTimePoint
                                )
                                onResolve(ConflictResolution.AdjustNeighbor(adjusted))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("缩短前一条记录")
                        }
                    }
                    is TimeConflict.OverlapWithNext -> {
                        // 选项1：缩短当前记录（提前结束时间）
                        Button(
                            onClick = {
                                val adjusted = conflict.currentPiece.copy(
                                    timePoint = conflict.nextPiece.fromTimePoint
                                )
                                onResolve(ConflictResolution.AdjustCurrent(adjusted))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("缩短当前记录（消除重叠）")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // 选项2：缩短后一条记录
                        OutlinedButton(
                            onClick = {
                                val adjusted = conflict.nextPiece.copy(
                                    fromTimePoint = conflict.currentPiece.timePoint
                                )
                                onResolve(ConflictResolution.AdjustNeighbor(adjusted))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("缩短后一条记录")
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("返回编辑")
            }
        }
    )
}

/**
 * 时间片段卡片
 */
@Composable
fun TimePieceCard(
    timePiece: TimePiece,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = convertTimeFormat(timePiece.timePoint, "M/d HH:mm"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = convertDurationFormat(
                        timePiece.timePoint - timePiece.fromTimePoint,
                        "%d时%d分"
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < timePiece.emotion) {
                                Color(0xFFFFD700)
                            } else {
                                Color.Gray.copy(alpha = 0.2f)
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = timePiece.mainEvent + 
                       if (timePiece.subEvent.isNotEmpty()) "：${timePiece.subEvent}" else "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (timePiece.lastTimeRecord.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "💭 ${timePiece.lastTimeRecord}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
