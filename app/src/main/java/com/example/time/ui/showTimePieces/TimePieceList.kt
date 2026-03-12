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
import com.example.time.ui.timeRecord.SimpleTimePieceEditDialog

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
 * 使用简化的编辑对话框，清晰显示影响
 */
@Composable
fun TimePieceList(
    timePieces: List<TimePiece>,
    viewModel: TimeViewModel? = null
) {
    var editingPiece by remember { mutableStateOf<TimePiece?>(null) }
    
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
    
    // 编辑对话框（新版简化组件）
    if (viewModel != null && editingPiece != null) {
        val piece = editingPiece!!
        val pieceIndex = timePieces.indexOf(piece)
        
        // 列表是倒序的：index小=时间晚, index大=时间早
        val laterPiece = if (pieceIndex > 0) timePieces[pieceIndex - 1] else null  // 时间更晚
        val earlierPiece = if (pieceIndex < timePieces.size - 1) timePieces[pieceIndex + 1] else null  // 时间更早
        
        SimpleTimePieceEditDialog(
            currentPiece = piece,
            earlierPiece = earlierPiece,
            laterPiece = laterPiece,
            onSave = { updated, adjustedEarlier, adjustedLater ->
                // 保存当前记录
                viewModel.updateTimePiece(updated)
                
                // 保存被调整的相邻记录（如果有）
                adjustedEarlier?.let { viewModel.updateTimePiece(it) }
                adjustedLater?.let { viewModel.updateTimePiece(it) }
                
                editingPiece = null
            },
            onDelete = { deleted ->
                // 删除记录时，需要填补时间空隙
                if (earlierPiece != null && laterPiece != null) {
                    // 中间记录：让时间更早的记录延长到时间更晚记录的开始
                    val adjustedEarlier = earlierPiece.copy(timePoint = laterPiece.fromTimePoint)
                    viewModel.updateTimePiece(adjustedEarlier)
                } else if (earlierPiece != null && laterPiece == null) {
                    // 最新记录：让时间更早的记录延长到被删除记录的结束时间
                    val adjustedEarlier = earlierPiece.copy(timePoint = deleted.timePoint)
                    viewModel.updateTimePiece(adjustedEarlier)
                } else if (earlierPiece == null && laterPiece != null) {
                    // 最早记录：让时间更晚记录的开始时间提前
                    val adjustedLater = laterPiece.copy(fromTimePoint = deleted.fromTimePoint)
                    viewModel.updateTimePiece(adjustedLater)
                }
                
                viewModel.deleteTimePiece(deleted)
                editingPiece = null
            },
            onCancel = { editingPiece = null }
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
