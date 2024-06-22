package com.example.time.ui.showTimePieces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertDurationFormat
import com.example.time.logic.utils.convertTimeFormat


@Composable
fun TimePieceListColumn(timePieces: List<TimePiece>) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        for (timePiece in timePieces) {
            TimePieceCard(timePiece = timePiece)
        }
    }
}
@Composable
fun TimePieceList(timePieces: List<TimePiece>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        items(timePieces) { timePiece ->
            TimePieceCard(timePiece = timePiece, modifier = Modifier.padding(horizontal = 8.dp))
        }
    }

//    LazyColumn(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(150.dp) // 设置固定高度
//    ) {
//        items(timePieces) { timePiece ->
////            Text(text = "${timePiece.mainEvent} - ${timePiece.subEvent}")
//            TimePieceCard(timePiece = timePiece)
//        }
//    }
}

@Composable
fun TimePieceCard(timePiece: TimePiece, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(2.dp)
        ) {

            Row {
                Text(
                    text = convertTimeFormat(
                        timePiece.timePoint,
                        "M/d  HH:mm"
//                        (if (convertTimeFormat(timePiece.fromTimePoint, "M/d") == convertTimeFormat(
//                                timePiece.timePoint,
//                                "M/d"
//                            )
//                        ) "HH:mm" else "M/d  HH:mm")
                    ),
                    Modifier.weight(1f), textAlign = TextAlign.Start
                )
                Text(
                    text = convertDurationFormat(
                        timePiece.timePoint - timePiece.fromTimePoint,
                        "%d时%d分"
                    ),
                    Modifier.weight(1f), textAlign = TextAlign.Center
                )
                Text(
                    "\u2605".repeat(timePiece.emotion),
                    Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    color = Color.Yellow,
                )
            }
            Text(
                timePiece.mainEvent + (if (timePiece.subEvent.isEmpty()) "" else "：${timePiece.subEvent}")
            )
            if (timePiece.lastTimeRecord.isNotEmpty()) {
                Text(timePiece.lastTimeRecord)
            }
        }
    }
}