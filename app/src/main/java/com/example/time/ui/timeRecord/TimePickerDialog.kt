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

@Composable
fun TimePickerDialog(latestTime: Long, onTimeSelected: (Long) -> Unit, onCancel: () -> Unit) {
    val latestTime: Long by remember { mutableStateOf(latestTime) }
    var selectedTime:Long by remember { mutableStateOf(System.currentTimeMillis()) }
    val nowTime = System.currentTimeMillis()
    var ratio by remember { mutableStateOf(0f) }

    AlertDialog(
        onDismissRequest = { /* 点击外部区域关闭对话框 */ },
        title = { Text(text = "选择时间") },
        text = {
            Column {
                Slider(
                    value = ratio,
                    onValueChange = {
                        ratio = it
                        selectedTime = (it/1000f * (System.currentTimeMillis() - latestTime) + latestTime).toLong()
                    },
                    valueRange = 0f..1000f,
                    steps = 1000,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                val timeString = convertTimeFormat(selectedTime).substring(5)
                val latestTimeString = convertTimeFormat(latestTime).substring(5)
                Text(text = "from $latestTimeString to $timeString", fontSize = 16.sp)
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