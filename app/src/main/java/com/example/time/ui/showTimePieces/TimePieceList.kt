/**
 * TimePieceList - 时间片段列表组件
 * 支持点击编辑功能
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

@Composable
fun TimePieceListColumn(timePieces: List<TimePiece>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (timePiece in timePieces) {
            TimePieceCard(timePiece = timePiece)
        }
    }
}

@Composable
fun TimePieceList(
    timePieces: List<TimePiece>,
    viewModel: TimeViewModel? = null
) {
    var editingPiece by remember { mutableStateOf<TimePiece?>(null) }
    
    // 时间冲突状态：存储冲突信息
    var showConflictDialog by remember { mutableStateOf(false) }
    var conflictMessage by remember { mutableStateOf("") }
    var conflictType by remember { mutableStateOf("") } // "gap" or "overlap"
    var pendingPiece by remember { mutableStateOf<TimePiece?>(null) }
    var neighborPiece by remember { mutableStateOf<TimePiece?>(null) }
    var adjustedNeighbor by remember { mutableStateOf<TimePiece?>(null) }
    var adjustedCurrent by remember { mutableStateOf<TimePiece?>(null) }
    
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
    
    // 编辑对话框
    if (viewModel != null && editingPiece != null) {
        val piece = editingPiece!!
        val pieceIndex = timePieces.indexOf(piece)
        
        val nextPiece = if (pieceIndex > 0) timePieces[pieceIndex - 1] else null
        val prevPiece = if (pieceIndex < timePieces.size - 1) timePieces[pieceIndex + 1] else null
        
        val minTime = prevPiece?.fromTimePoint ?: 0L
        val maxTime = nextPiece?.timePoint ?: System.currentTimeMillis()
        
        TimePieceEditDialog(
            timePiece = piece,
            onSave = { updated ->
                // 检查与前一条记录的冲突
                if (prevPiece != null) {
                    when {
                        updated.fromTimePoint < prevPiece.timePoint -> {
                            // 重叠
                            conflictType = "overlap_prev"
                            conflictMessage = "与前一条记录时间重叠\n重叠时段：${convertTimeFormat(updated.fromTimePoint, "HH:mm")} ~ ${convertTimeFormat(prevPiece.timePoint, "HH:mm")}"
                            pendingPiece = updated
                            neighborPiece = prevPiece
                            adjustedCurrent = updated.copy(fromTimePoint = prevPiece.timePoint)
                            adjustedNeighbor = prevPiece.copy(timePoint = updated.fromTimePoint)
                            showConflictDialog = true
                            return@TimePieceEditDialog
                        }
                        updated.fromTimePoint > prevPiece.timePoint -> {
                            // 间隙
                            conflictType = "gap_prev"
                            val duration = convertDurationFormat(updated.fromTimePoint - prevPiece.timePoint, "%d时%d分")
                            conflictMessage = "与前一条记录之间有时间间隙\n间隙时长：$duration"
                            pendingPiece = updated
                            neighborPiece = prevPiece
                            adjustedCurrent = updated.copy(fromTimePoint = prevPiece.timePoint)
                            adjustedNeighbor = prevPiece.copy(timePoint = updated.fromTimePoint)
                            showConflictDialog = true
                            return@TimePieceEditDialog
                        }
                    }
                }
                
                // 检查与后一条记录的冲突
                if (nextPiece != null) {
                    when {
                        updated.timePoint > nextPiece.fromTimePoint -> {
                            // 重叠
                            conflictType = "overlap_next"
                            conflictMessage = "与后一条记录时间重叠\n重叠时段：${convertTimeFormat(nextPiece.fromTimePoint, "HH:mm")} ~ ${convertTimeFormat(updated.timePoint, "HH:mm")}"
                            pendingPiece = updated
                            neighborPiece = nextPiece
                            adjustedCurrent = updated.copy(timePoint = nextPiece.fromTimePoint)
                            adjustedNeighbor = nextPiece.copy(fromTimePoint = updated.timePoint)
                            showConflictDialog = true
                            return@TimePieceEditDialog
                        }
                        updated.timePoint < nextPiece.fromTimePoint -> {
                            // 间隙
                            conflictType = "gap_next"
                            val duration = convertDurationFormat(nextPiece.fromTimePoint - updated.timePoint, "%d时%d分")
                            conflictMessage = "与后一条记录之间有时间间隙\n间隙时长：$duration"
                            pendingPiece = updated
                            neighborPiece = nextPiece
                            adjustedCurrent = updated.copy(timePoint = nextPiece.fromTimePoint)
                            adjustedNeighbor = nextPiece.copy(fromTimePoint = updated.timePoint)
                            showConflictDialog = true
                            return@TimePieceEditDialog
                        }
                    }
                }
                
                // 无冲突，直接保存
                viewModel.updateTimePiece(updated)
                editingPiece = null
            },
            onDelete = { deleted ->
                viewModel.deleteTimePiece(deleted)
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
    
    // 时间冲突对话框
    if (showConflictDialog) {
        AlertDialog(
            onDismissRequest = { 
                showConflictDialog = false 
            },
            title = {
                Text(
                    text = if (conflictType.contains("overlap")) "⚠️ 时间重叠" else "⚠️ 时间间隙",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(conflictMessage)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("请选择处理方式：", fontWeight = FontWeight.Medium)
                }
            },
            confirmButton = {
                Column {
                    // 选项1：调整当前记录
                    Button(
                        onClick = {
                            adjustedCurrent?.let { viewModel?.updateTimePiece(it) }
                            showConflictDialog = false
                            editingPiece = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (conflictType.contains("overlap")) "缩短当前记录" else "延长当前记录")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // 选项2：调整相邻记录
                    OutlinedButton(
                        onClick = {
                            pendingPiece?.let { viewModel?.updateTimePiece(it) }
                            adjustedNeighbor?.let { viewModel?.updateTimePiece(it) }
                            showConflictDialog = false
                            editingPiece = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (conflictType.contains("prev")) "调整前一条记录" else "调整后一条记录")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showConflictDialog = false }) {
                    Text("返回编辑")
                }
            }
        )
    }
}

@Composable
fun TimePieceCard(
    timePiece: TimePiece,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    text = convertDurationFormat(timePiece.timePoint - timePiece.fromTimePoint, "%d时%d分"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < timePiece.emotion) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.2f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = timePiece.mainEvent + if (timePiece.subEvent.isNotEmpty()) "：${timePiece.subEvent}" else "",
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
