package com.example.time.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.internal.isLiveLiteralsEnabled
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertDurationFormat
import com.example.time.ui.showTimePieces.ButtonToShowEventFeelingActivity
import com.example.time.ui.showTimePieces.TimePieceList
import com.example.time.ui.showTimePieces.TimePieceListColumn

@Composable
fun ExpandableList(timePieces: List<TimePiece>, colorMap: Map<String, Color>) {
    val timePiecesByMainEvent = timePieces
        .groupBy { it.mainEvent }
        .toList()
        .sortedByDescending { it -> it.second.sumOf { it.timePoint - it.fromTimePoint }}
        .toMap()
    val timePiecesLength = timePiecesByMainEvent.mapValues { entry ->
        entry.value.sumOf { it.timePoint - it.fromTimePoint }
    }
    // repeat(timePiecesByMainEvent.size) 会报错，暂时处理为1000
    val expandedStates = remember { mutableStateListOf<Boolean>().apply { repeat(1000) { add(false) } } }
    val maxTimeLength = timePiecesLength.maxByOrNull { it.value }?.value
    // 找到最大的 value
    val maxEntry = timePiecesLength.maxByOrNull { it.value }
    val maxValue = maxEntry?.value ?: 1L // 确保最大值不为 0

    // 构建新的 Map<String, Float>
    val normalizedTimePiecesLength = timePiecesLength.mapValues { entry ->
        entry.value.toFloat() / maxValue.toFloat()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        itemsIndexed(timePiecesByMainEvent.entries.toList()) { index, entry ->
            val key = entry.key
            val value = entry.value
            // Assuming the index corresponds to the index of the other lists
            if (maxTimeLength != null) {
                ExpandableListItem(
                    item = key,
                    details = entry.value,
                    isExpanded = expandedStates[index],
//                    isExpanded = false,
                    progress = normalizedTimePiecesLength[key] ?: 1F,
                    color = colorMap[key] ?: Color.Blue,
                    onClick = { expandedStates[index] = !expandedStates[index] }
                )
            }
        }
    }
}

@Composable
fun ExpandableListItem(
    item: String,
    details: List<TimePiece>,
    isExpanded: Boolean,
    progress: Float,
    color: Color,
    onClick: () -> Unit
) {
    val timePiecesBySubEvent = details
        .groupBy { it.subEvent }
    val textColor = getContrastingColor(color)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // To make sure the background box does not expand beyond the text
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(color)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
//                    .padding(8.dp)
            ) {
                val shadowColor = if (textColor == Color.White) Color.Black else Color.White
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 0.dp, end = 0.dp)) {
                    Text(
                        text = item,
                        fontSize = 13.sp,
                        color = textColor,
                        style = TextStyle(
                            shadow = Shadow(
                                color = shadowColor,
                                blurRadius = 3f,
                                offset = Offset(1f, 1f)
                            )
                        ),
                        modifier = Modifier.weight(1f) // Make the text take up the remaining space
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ButtonToShowEventFeelingActivity(modifier = Modifier.size(width = 15.dp, height = 15.dp), item, color)
                }
            }
        }
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                TimePieceListColumn(timePieces = details)
            }
        }
//        Divider()
    }
}