/**
 * 提交确认对话框组件
 * 
 * 功能说明：
 * - 在用户点击提交按钮后显示确认对话框
 * - 允许用户修改事件的结束时间
 * - 支持添加媒体附件
 * - 显示完整的记录预览，让用户确认无误后再提交
 * 
 * 开发原则：
 * - 所有数据仅在本地处理，不上传网络
 * - 增强用户交互体验，减少误操作
 */
package com.example.time.ui.timeRecord

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertTimeFormat
import com.example.time.ui.theme.EmotionColors
import com.example.time.ui.components.MediaPicker

/**
 * 提交确认对话框
 * 
 * @param timePiece 待提交的时间片段数据
 * @param onConfirm 确认提交回调，参数为(结束时间, 媒体路径列表)
 * @param onCancel 取消提交回调
 * @param latestTime 最早可选时间（上一条记录的时间点）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitConfirmDialog(
    timePiece: TimePiece,
    onConfirm: (finishedTime: Long, mediaPaths: List<String>) -> Unit,
    onCancel: () -> Unit,
    latestTime: Long
) {
    // 是否正在编辑结束时间
    var isEditingTime by remember { mutableStateOf(false) }
    // 当前选择的结束时间
    var selectedFinishTime by remember { mutableStateOf(timePiece.timePoint) }
    // 媒体附件列表
    var mediaPaths by remember { mutableStateOf(emptyList<String>()) }
    
    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 标题
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📝 确认记录",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                
                // 可滚动内容区
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 事件名称
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📌 事件",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = timePiece.mainEvent + 
                                       if (timePiece.subEvent.isNotEmpty()) "：${timePiece.subEvent}" else "",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 时间区间
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
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("开始：", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = convertTimeFormat(timePiece.fromTimePoint, "yyyy/MM/dd HH:mm"),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("结束：", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = convertTimeFormat(selectedFinishTime, "yyyy/MM/dd HH:mm"),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { isEditingTime = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "修改结束时间",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 心情
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("心情：", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (index < timePiece.emotion) 
                                    EmotionColors.getColorForStar(timePiece.emotion) 
                                else Color.Gray.copy(alpha = 0.3f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    // 体验记录
                    if (timePiece.lastTimeRecord.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "💭 体验记录",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = timePiece.lastTimeRecord,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 媒体附件
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📷 媒体附件",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (mediaPaths.isNotEmpty()) {
                                    Text(
                                        text = "${mediaPaths.size}张",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            MediaPicker(
                                mediaPaths = mediaPaths,
                                onMediaAdded = { path ->
                                    if (mediaPaths.size < 9) {
                                        mediaPaths = mediaPaths + path
                                    }
                                },
                                onMediaRemoved = { path ->
                                    mediaPaths = mediaPaths - path
                                },
                                maxMedia = 9
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 底部按钮
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
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("取消")
                        }
                        
                        Button(
                            onClick = { onConfirm(selectedFinishTime, mediaPaths) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                   ) {
                            Text("✓ 确认记录")
                        }
                    }
                }
            }
        }
    }
    
    // 时间选择器
    if (isEditingTime) {
        TimePickerDialog(
            latestTime = latestTime,
            onTimeSelected = { newTime ->
                selectedFinishTime = newTime
                isEditingTime = false
            },
            onCancel = { isEditingTime = false }
        )
    }
}