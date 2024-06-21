package com.example.time.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.ui.showTimePieces.TimePieceList

@Composable
fun ExpandableList(timePieces: List<TimePiece>, colorMap: MutableMap<String, Color>) {
    val timePiecesByMainEvent = timePieces
        .groupBy { it.mainEvent }
    val timePiecesLength = timePiecesByMainEvent.mapValues { entry ->
        entry.value.sumOf { it.timePoint - it.fromTimePoint }
    }
    val expandedStates = remember { mutableStateListOf<Boolean>().apply { repeat(timePiecesLength.size) { add(false) } } }
    val maxTimeLength = timePiecesLength.maxByOrNull { it.value }?.value
//    progressValues =
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
            .padding(16.dp)
    ) {
        itemsIndexed(timePiecesByMainEvent.entries.toList()) { index, entry ->
            val key = entry.key
            val value = entry.value
            // Assuming the index corresponds to the index of the other lists
            if (maxTimeLength != null) {
                ExpandableListItem(
                    item = key,
                    details = value,
                    isExpanded = expandedStates[index],
                    progress = normalizedTimePiecesLength[key]!!,
                    color = colorMap[key] ?: Color.Black,
                    onClick = { expandedStates[index] = !expandedStates[index] }
                )
            }
        }
    }

//    LazyColumn(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        timePiecesByMainEvent.forEach { (key, value) ->
//            ExpandableListItem(
//                item = items[],
//                details = details[],
//                isExpanded = expandedStates[],
//                progress = progressValues[],
//                color = colors[],
//                onClick = { expandedStates[] = !expandedStates[] }
//            )
//        }
//
//    }
}

//items(items.size) { index ->
//    ExpandableListItem(
//        item = items[index],
//        details = details[index],
//        isExpanded = expandedStates[index],
//        progress = progressValues[index],
//        color = colors[index],
//        onClick = { expandedStates[index] = !expandedStates[index] }
//    )
//}

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
            Text(
                text = item,
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
        if (isExpanded) {
            TimePieceList(timePieces = details)
        }
        Divider()
    }
}