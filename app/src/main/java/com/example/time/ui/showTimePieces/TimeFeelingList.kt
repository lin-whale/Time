package com.example.time.ui.showTimePieces

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertDurationFormat
import com.example.time.ui.activity.ShowEventFeelingActivity
import com.example.time.ui.activity.ShowTimePiecesActivity

@Composable
fun TimeFeelingList(timePieceList: List<TimePiece>) {
    val (selectedEmotion, setSelectedEmotion) = remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 一行按钮，每个按钮对应一个emotion数值
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                val isSelected = selectedEmotion == i
                Button(
                    onClick = { setSelectedEmotion(i) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFF2D0050) else Color(0xFFFFB0C8))
                ) {
                    Text(text = "⭐️$i", fontSize = 10.sp, color = if (isSelected) Color.White else Color.Black)
                }
            }
        }

        // 显示emotion=num的所有TimePiece，并按照相同的mainEvent分组
        val filteredTimePieces = timePieceList.filter { it.emotion == selectedEmotion }
        val groupedTimePieces = filteredTimePieces.groupBy { it.mainEvent }
            .toList() // 转换为列表
            .sortedByDescending { (_, timePieces) ->
                timePieces.sumOf { (it.timePoint - it.fromTimePoint).toInt() } // 根据时间总和由大到小排序
            }
            .toMap() // 转换回映射

        LazyColumn {
            items(groupedTimePieces.keys.toList()) { mainEvent ->
                val timePieces = groupedTimePieces[mainEvent] ?: emptyList()
                val totalTime = timePieces.sumOf { (it.timePoint - it.fromTimePoint).toInt() }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 20.dp, end = 20.dp)) {
                    ButtonToShowEventFeelingActivity(modifier = Modifier.size(width = 15.dp, height = 15.dp), mainEvent, Color(0xFFDEB7FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mainEvent,
                        modifier = Modifier.weight(4f),
                        textAlign = TextAlign.Start,
                        style = TextStyle(fontSize = 16.sp)
                    )
                    Text(
                        text = convertDurationFormat(totalTime.toLong()),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        style = TextStyle(fontSize = 16.sp)
                    )
                }
            }
        }
    }
}

@Composable
fun ButtonToShowEventFeelingActivity(modifier: Modifier, mainEvent:String, color: Color) {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 处理从新的 Activity 返回的结果
        }

    Button(onClick = {
        val intent = Intent(context, ShowEventFeelingActivity::class.java)
        intent.putExtra("mainEvent", mainEvent)
        activityResultLauncher.launch(intent)
    }, modifier = modifier,
//    Modifier.size(width = 15.dp, height = 15.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text("")
    }
}