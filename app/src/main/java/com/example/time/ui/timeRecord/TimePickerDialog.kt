package com.example.time.ui.timeRecord

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    // 直接使用参数，不用 remember 缓存
    val minTime = latestTime
    val upperBound = maxTime
    
    // 计算时间范围
    val timeRange = upperBound - minTime
    
    // 计算初始时间：使用传入的 initialTime（需约束在范围内），否则默认中间值
    val effectiveInitialTime = when {
        initialTime != null && initialTime in minTime..upperBound -> initialTime
        initialTime != null && initialTime < minTime -> minTime
        initialTime != null && initialTime > upperBound -> upperBound
        else -> (minTime + upperBound) / 2
    }
    
    // 计算初始 ratio（0~1000）
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

    AlertDialog(
        onDismissRequest = { },
        title = { Text(text = "选择时间") },
        text = {
            Column {
                if (timeRange > 0) {
                    Slider(
                        value = ratio,
                        onValueChange = {
                            ratio = it
                            selectedTime = (it / 1000f * timeRange + minTime).toLong()
                        },
                        valueRange = 0f..1000f,
                        steps = 1000,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    Text(
                        text = "⚠️ 时间范围为零，无法调整",
                        color = androidx.compose.ui.graphics.Color.Red
                    )
                }
                
                val minTimeStr = convertTimeFormat(minTime, "MM/dd HH:mm")
                val maxTimeStr = convertTimeFormat(upperBound, "MM/dd HH:mm")
                val selectedStr = convertTimeFormat(selectedTime, "MM/dd HH:mm")
                
                Text(text = "范围: $minTimeStr ~ $maxTimeStr", fontSize = 12.sp)
                Text(text = "选择: $selectedStr", fontSize = 16.sp)
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
