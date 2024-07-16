package com.example.time.ui.showTimePieces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertDurationFormat

@Composable
fun TimeFeelingListByEvent(timePieceList: List<TimePiece>) {
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

        // 显示emotion=num的所有TimePiece
        var filteredTimePieces = timePieceList.filter { it.emotion == selectedEmotion }
        filteredTimePieces = filteredTimePieces.reversed()
        TimePieceListForEvent(filteredTimePieces)
    }
}