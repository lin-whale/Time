/**
 * TimePieceList - 时间片段列表组件
 * 支持点击编辑功能，自动保持时间连续性
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

/**
 * 编辑影响分析结果
 */
data class EditImpactAnalysis(
    val piecesToDelete: List<TimePiece>,      // 将被删除的记录
    val piecesToAdjust: List<Pair<TimePiece, TimePiece>>,  // (原记录, 调整后) 
    val hasImpact: Boolean                     // 是否有影响
)

/**
 * 分析编辑操作对其他记录的影响
 */
fun analyzeEditImpact(
    original: TimePiece,
    updated: TimePiece,
    allPieces: List<TimePiece>,
    pieceIndex: Int
): EditImpactAnalysis {
    val toDelete = mutableListOf<TimePiece>()
    val toAdjust = mutableListOf<Pair<TimePiece, TimePiece>>()
    
    // 1. 分析开始时间提前的影响（影响之前的记录）
    if (updated.fromTimePoint < original.fromTimePoint) {
        for (i in (pieceIndex + 1) until allPieces.size) {
            val earlierPiece = allPieces[i]
            
            if (earlierPiece.timePoint <= updated.fromTimePoint) {
                break
            } else if (earlierPiece.fromTimePoint >= updated.fromTimePoint) {
                toDelete.add(earlierPiece)
            } else {
                val adjusted = earlierPiece.copy(timePoint = updated.fromTimePoint)
                toAdjust.add(earlierPiece to adjusted)
                break
            }
        }
    }
    
    // 2. 分析结束时间延后的影响（影响之后的记录）
    if (updated.timePoint > original.timePoint) {
        for (i in (pieceIndex - 1) downTo 0) {
            val laterPiece = allPieces[i]
            
            if (laterPiece.fromTimePoint >= updated.timePoint) {
                break
            } else if (laterPiece.timePoint <= updated.timePoint) {
                toDelete.add(laterPiece)
            } else {
                val adjusted = laterPiece.copy(fromTimePoint = updated.timePoint)
                toAdjust.add(laterPiece to adjusted)
                break
            }
        }
    }
    
    return EditImpactAnalysis(
        piecesToDelete = toDelete,
        piecesToAdjust = toAdjust,
        hasImpact = toDelete.isNotEmpty() || toAdjust.isNotEmpty()
    )
}

/**
 * 时间片段列表
 * 编辑保存时自动调整相邻记录，保持时间连续不重叠
 */
@Composable
fun TimePieceList(
    timePieces: List<TimePiece>,
    viewModel: TimeViewModel? = null
) {
    var editingPiece by remember { mutableStateOf<TimePiece?>(null) }
    
    // 待确认的编辑操作
    var pendingUpdate by remember { mutableStateOf<TimePiece?>(null) }
    var pendingImpact by remember { mutableStateOf<EditImpactAnalysis?>(null) }
    var pendingPieceIndex by remember { mutableStateOf(-1) }
    
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
        
        val minTime = prevPiece?.timePoint ?: 0L
        val maxTime = nextPiece?.fromTimePoint ?: System.currentTimeMillis()
        
        TimePieceEditDialog(
            timePiece = piece,
            onSave = { updated ->
                // 分析影响
                val impact = analyzeEditImpact(piece, updated, timePieces, pieceIndex)
                
                if (impact.hasImpact) {
                    // 有影响，显示确认对话框
                    pendingUpdate = updated
                    pendingImpact = impact
                    pendingPieceIndex = pieceIndex
                    editingPiece = null
                } else {
                    // 无影响，直接保存
                    viewModel.updateTimePiece(updated)
                    editingPiece = null
                }
            },
            onDelete = { deleted ->
                if (prevPiece != null && nextPiece != null) {
                    val adjustedPrev = prevPiece.copy(timePoint = nextPiece.fromTimePoint)
                    viewModel.updateTimePiece(adjustedPrev)
                } else if (prevPiece != null && nextPiece == null) {
                    val adjustedPrev = prevPiece.copy(timePoint = deleted.timePoint)
                    viewModel.updateTimePiece(adjustedPrev)
                } else if (prevPiece == null && nextPiece != null) {
                    val adjustedNext = nextPiece.copy(fromTimePoint = deleted.fromTimePoint)
                    viewModel.updateTimePiece(adjustedNext)
                }
                
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
    
    // 影响确认对话框
    if (pendingUpdate != null && pendingImpact != null && viewModel != null) {
        val impact = pendingImpact!!
        val updated = pendingUpdate!!
        
        AlertDialog(
            onDismissRequest = {
                pendingUpdate = null
                pendingImpact = null
            },
            title = { 
                Text(
                    text = "⚠️ 修改将影响其他记录",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column {
                    Text(
                        text = "此次修改将导致以下变更：",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 显示将被删除的记录
                    if (impact.piecesToDelete.isNotEmpty()) {
                        Text(
                            text = "🗑️ 将被删除的记录（${impact.piecesToDelete.size}条）：",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                        impact.piecesToDelete.forEach { piece ->
                            Text(
                                text = "• ${piece.mainEvent}${if (piece.subEvent.isNotEmpty()) "：${piece.subEvent}" else ""}\n  ${convertTimeFormat(piece.fromTimePoint, "MM/dd HH:mm")} → ${convertTimeFormat(piece.timePoint, "HH:mm")}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // 显示将被调整的记录
                    if (impact.piecesToAdjust.isNotEmpty()) {
                        Text(
                            text = "✏️ 将被调整的记录（${impact.piecesToAdjust.size}条）：",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        impact.piecesToAdjust.forEach { (original, adjusted) ->
                            val timeChange = if (original.fromTimePoint != adjusted.fromTimePoint) {
                                "开始时间 ${convertTimeFormat(original.fromTimePoint, "HH:mm")} → ${convertTimeFormat(adjusted.fromTimePoint, "HH:mm")}"
                            } else {
                                "结束时间 ${convertTimeFormat(original.timePoint, "HH:mm")} → ${convertTimeFormat(adjusted.timePoint, "HH:mm")}"
                            }
                            Text(
                                text = "• ${original.mainEvent}：$timeChange",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // 执行删除
                        impact.piecesToDelete.forEach { piece ->
                            viewModel.deleteTimePiece(piece)
                        }
                        // 执行调整
                        impact.piecesToAdjust.forEach { (_, adjusted) ->
                            viewModel.updateTimePiece(adjusted)
                        }
                        // 保存当前记录
                        viewModel.updateTimePiece(updated)
                        
                        pendingUpdate = null
                        pendingImpact = null
                    }
                ) {
                    Text("确认修改")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        pendingUpdate = null
                        pendingImpact = null
                    }
                ) {
                    Text("取消")
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
