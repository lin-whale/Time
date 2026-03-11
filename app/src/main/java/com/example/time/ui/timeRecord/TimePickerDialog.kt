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
 * @param maxTime 最大可选时间（默认为当前时间）
 * @param onTimeSelected 选择时间后的回调
 * @param onCancel 取消回调
 */
@Composable
fun TimePickerDialog(
    latestTime: Long, 
    maxTime: Long = System.currentTimeMillis(),  // 新增：最大时间限制
    onTimeSelected: (Long) -> Unit, 
    onCancel: () -> Unit
) {
    val minTime: Long by remember { mutableStateOf(latestTime) }
    val upperBound: Long by remember { mutableStateOf(maxTime) }
    
    // 默认选择中间时间
    var selectedTime: Long by remember { mutableStateOf((minTime + upperBound) / 2) }
    var ratio by remember { mutableStateOf(500f) }  // 默认滑到中间

    AlertDialog(
        onDismissRequest = { /* 点击外部区域关闭对话框 */ },
        title = { Text(text = "选择时间") },
        text = {
            Column {
                Slider(
                    value = ratio,
                    onValueChange = {
                        ratio = it
                        // 根据滑动比例计算时间，范围是 minTime 到 upperBound
                        selectedTime = (it / 1000f * (upperBound - minTime) + minTime).toLong()
                    },
                    valueRange = 0f..1000f,
                    steps = 1000,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                val timeString = convertTimeFormat(selectedTime).substring(5)
                val minTimeString = convertTimeFormat(minTime).substring(5)
                val maxTimeString = convertTimeFormat(upperBound).substring(5)
                Text(text = "范围: $minTimeString ~ $maxTimeString", fontSize = 12.sp)
                Text(text = "选择: $timeString", fontSize = 16.sp)
            }
        },
        confirmButton = {
            Button(onClick = { onTimeSelected(selectedTime) }) {
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
