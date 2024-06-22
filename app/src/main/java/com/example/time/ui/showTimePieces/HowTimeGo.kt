package com.example.time.ui.showTimePieces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.time.logic.model.TimePiece
import com.example.time.ui.utils.getRandomColor
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HowTimeGo(timePieces: List<TimePiece>) {
    // Step 1: 计算每种mainEvent的时间长度总和
    val timeSumsByEmotion = timePieces
        .groupBy { it.emotion }
        .mapValues { entry ->
            entry.value.sumOf { it.timePoint - it.fromTimePoint }
        }
        .toList()
        .sortedBy { it.first }
        .toMap()

    // Step 1: 创建颜色映射
    val colorMap = mutableMapOf<Int, Color>()
    timeSumsByEmotion.keys.forEach { emotion ->
        colorMap[emotion] = getRandomColor()
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
    Canvas(modifier = Modifier.size(height=300.dp, width = 400.dp)) {
        val diameter = size.minDimension
        val radius = diameter / 3f
        val center = Offset(size.width / 2, size.height / 2)
        val totalSum = timeSumsByEmotion.values.sum()
        var startAngle = -90f

        timeSumsByEmotion.forEach { (mainEvent, sum) ->
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

            // 绘制文本标签
            drawContext.canvas.nativeCanvas.drawText(
                "⭐️".repeat(mainEvent),
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
}

// 在外部定义 labelPaint
private val labelPaint = android.graphics.Paint().apply {
    isAntiAlias = true
    textSize = 30f // 设置文本大小
    color = android.graphics.Color.BLACK // 设置文本颜色
    textAlign = android.graphics.Paint.Align.CENTER
}
