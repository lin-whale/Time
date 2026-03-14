package com.example.time.ui.timeRecord

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.utils.convertTimeFormat

/**
 * 时间选择对话框
 * 
 * @param latestTime 最小可选时间（起始时间）
 * @param maxTime 最大可选时间
 * @param initialTime 初始选中时间（可选，默认为中间值）
 * @param onTimeSelected 选择时间后的回调
 * @param onCancel 取消回调
 */
@Composable
fun TimePickerDialog(
    latestTime: Long, 
    maxTime: Long = System.currentTimeMillis(),
    initialTime: Long? = null,
    onTimeSelected: (Long) -> Unit, 
    onCancel: () -> Unit
) {
    val minTime = latestTime
    val upperBound = maxTime
    val timeRange = upperBound - minTime
    
    val effectiveInitialTime = when {
        initialTime != null && initialTime in minTime..upperBound -> initialTime
        initialTime != null && initialTime < minTime -> minTime
        initialTime != null && initialTime > upperBound -> upperBound
        else -> (minTime + upperBound) / 2
    }
    
    val initialRatio = if (timeRange > 0) {
        ((effectiveInitialTime - minTime).toFloat() / timeRange * 1000f).coerceIn(0f, 1000f)
    } else {
        500f
    }
    
    var selectedTime by remember(minTime, upperBound, initialTime) { 
        mutableStateOf(effectiveInitialTime) 
    }
    var ratio by remember(minTime, upperBound, initialTime) { 
        mutableStateOf(initialRatio) 
    }
    
    // 调整时间的辅助函数
    fun adjustTime(minutesDelta: Int) {
        val newTime = selectedTime + minutesDelta * 60 * 1000L
        if (newTime in minTime..upperBound) {
            selectedTime = newTime
            ratio = ((newTime - minTime).toFloat() / timeRange * 1000f).coerceIn(0f, 1000f)
        }
    }

    AlertDialog(
        onDismissRequest = { },
        title = { Text(text = "选择时间") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 时间显示区域（放在顶部，增加与滑块的间距）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val selectedStr = convertTimeFormat(selectedTime, "MM/dd HH:mm")
                    Text(
                        text = selectedStr,
                        fontSize = 24.sp,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val minTimeStr = convertTimeFormat(minTime, "MM/dd HH:mm")
                    val maxTimeStr = convertTimeFormat(upperBound, "MM/dd HH:mm")
                    Text(
                        text = "范围: $minTimeStr ~ $maxTimeStr",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 增加间距，防止手指遮挡
                Spacer(modifier = Modifier.height(24.dp))
                
                // ±5分钟按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { adjustTime(-5) },
                        enabled = timeRange > 0 && selectedTime - 5 * 60 * 1000L >= minTime,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("-5分钟")
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    OutlinedButton(
                        onClick = { adjustTime(5) },
                        enabled = timeRange > 0 && selectedTime + 5 * 60 * 1000L <= upperBound,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+5分钟")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 滑动条区域
                if (timeRange > 0) {
                    Slider(
                        value = ratio,
                        onValueChange = {
                            ratio = it
                            selectedTime = (it / 1000f * timeRange + minTime).toLong()
                        },
                        valueRange = 0f..1000f,
                        steps = 1000,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                } else {
                    Text(
                        text = "⚠️ 时间范围为零，无法调整",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTimeSelected(selectedTime) },
                enabled = timeRange > 0
            ) {
                Text(text = "确认")
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text(text = "取消")
            }
        }
    )
}
