package com.example.time.ui.showTimePieces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertDurationFormat
import com.example.time.ui.utils.getRandomColor
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WhereTimeFly(timePieces: List<TimePiece>) {
    // Step 1: 计算每种mainEvent的时间长度总和
    val timeSumsByMainEvent = timePieces
        .groupBy { it.mainEvent }
        .mapValues { entry ->
            entry.value.sumOf { it.timePoint - it.fromTimePoint }
        }
        .toList()
        .sortedByDescending { it.second }
        .toMap()

    // Step 1: 创建颜色映射
    val colorMap = mutableMapOf<String, Color>()
    timeSumsByMainEvent.keys.forEach { mainEvent ->
        colorMap[mainEvent] = getRandomColor()
    }

    // Step 2: 绘制扇形图、标签和指向线
    // 辅助函数：计算文本标签的位置
    fun calculateLabelPosition(center: Offset, angle: Float, distance: Float): Offset {
        val labelOffsetX = center.x + distance * cos(Math.toRadians(angle.toDouble())).toFloat()
        val labelOffsetY = center.y + distance * sin(Math.toRadians(angle.toDouble())).toFloat()
        return Offset(labelOffsetX, labelOffsetY)
    }

    // 辅助函数：计算折线的起始点位置
    fun calculateLineStart(center: Offset, angle: Float, radius: Float): Offset {
        val lineStartX = center.x + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
        val lineStartY = center.y + radius * sin(Math.toRadians(angle.toDouble())).toFloat()
        return Offset(lineStartX, lineStartY)
    }

    // 辅助函数：计算折线的结束点位置
    fun calculateLineEnd(center: Offset, angle: Float, distance: Float): Offset {
        val lineEndX = center.x + distance * cos(Math.toRadians(angle.toDouble())).toFloat()
        val lineEndY = center.y + distance * sin(Math.toRadians(angle.toDouble())).toFloat()
        return Offset(lineEndX, lineEndY)
    }
    Canvas(modifier = Modifier.size(400.dp)) {
        val diameter = size.minDimension
        val radius = diameter / 3f
        val center = Offset(size.width / 2, size.height / 2)
        val totalSum = timeSumsByMainEvent.values.sum()
        var startAngle = -90f

        timeSumsByMainEvent.forEach { (mainEvent, sum) ->
            val sweepAngle = (sum.toFloat() / totalSum.toFloat()) * 360f
            val labelAngle = startAngle + sweepAngle / 2
            val labelOffset = calculateLabelPosition(center, labelAngle, radius + 60f) // 计算文本标签的位置
            val lineStart = calculateLineStart(center, labelAngle, radius) // 计算折线的起始点位置
            val lineEnd = calculateLineEnd(center, labelAngle, radius + 30f) // 计算折线的结束点位置

            // 绘制扇形块
            colorMap[mainEvent]?.let {
                drawArc(
                    color = it,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = center - Offset(radius, radius),
                    size = Size(radius * 2, radius * 2)
                )
            }

            // 绘制标签连接线
//            drawLine(
//                color = Color.Black,
//                start = lineStart,
//                end = lineEnd,
//                strokeWidth = 2f
//            )

            // 绘制文本标签
            drawContext.canvas.nativeCanvas.drawText(
                mainEvent,
                labelOffset.x,
                labelOffset.y,
                labelPaint
            )

            startAngle += sweepAngle
        }

        // 绘制中间的圆形
        val innerCircleRadius = radius / 2
        drawCircle(
            color = Color.White,
            center = center,
            radius = innerCircleRadius
        )
    }

    // Step 3: 绘制图例
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                timeSumsByMainEvent.forEach { (mainEvent, sum) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Box(
//                            modifier = Modifier
//                                .size(16.dp)
//                                .background(colorMap[mainEvent] ?: Color.Black)
//                        )
                        ButtonToShowEventFeelingActivity(
                            modifier = Modifier
                                    .size(width = 15.dp, height = 15.dp)
                                , mainEvent, colorMap[mainEvent] ?: Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = mainEvent,
                            modifier = Modifier.weight(3f),
                            textAlign = TextAlign.Start,
                            style = TextStyle(fontSize = 16.sp)
                        )
                        Text(
                            text = convertDurationFormat(sum.toLong()),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            style = TextStyle(fontSize = 16.sp)
                        )
                    }
                }
            }
        }
    }

}

// 在外部定义 labelPaint
private val labelPaint = android.graphics.Paint().apply {
    isAntiAlias = true
    textSize = 30f // 设置文本大小
    color = android.graphics.Color.BLACK // 设置文本颜色
    textAlign = android.graphics.Paint.Align.CENTER
}