/**
 * WhereTimeFly - 时间去向饼图
 * 
 * 改动说明：
 * - 修复除零错误：当没有记录或总时长为0时，添加保护逻辑
 * - 优化图表显示：空数据时显示友好提示
 * - 使用新的配色方案
 * 
 * Bug修复说明：
 * - 问题：开始使用时没有记录，计算百分比时 totalSum 为 0 导致除零错误
 * - 解决：在分母中添加保护值，并在空数据时显示提示信息
 */
package com.example.time.ui.showTimePieces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
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
import com.example.time.ui.theme.ChartColors
import com.example.time.ui.utils.ExpandableList
import com.example.time.ui.utils.getRandomColor
import kotlin.math.cos
import kotlin.math.sin

/**
 * 时间去向饼图组件
 * 显示各事件类型占用时间的比例分布
 * 
 * @param timePieces 时间片段列表
 */
@Composable
fun WhereTimeFly(timePieces: List<TimePiece>) {
    // ===== 空数据保护 =====
    // 修复：当没有记录时显示友好提示，避免后续计算出错
    if (timePieces.isEmpty()) {
        Box(
            modifier = Modifier
                .size(width = 400.dp, height = 300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📊",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "暂无数据",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "选择的时间范围内没有记录\n请先添加一些时间记录",
                    fontSize = 14.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    // Step 1: 计算每种mainEvent的时间长度总和
    val timeSumsByMainEvent = timePieces
        .groupBy { it.mainEvent }
        .mapValues { entry ->
            entry.value.sumOf { it.timePoint - it.fromTimePoint }
        }
        .toList()
        .sortedByDescending { it.second }
        .toMap()

    // Step 2: 创建颜色映射（使用新的配色方案）
    val colorMap = mutableMapOf<String, Color>()
    timeSumsByMainEvent.keys.forEachIndexed { index, mainEvent ->
        // 使用预定义的图表配色，更美观且易区分
        colorMap[mainEvent] = ChartColors.getColor(index)
    }

    // Step 3: 绘制扇形图
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
    
    Canvas(
        modifier = Modifier
            .size(width = 400.dp, height = 300.dp)
    ) {
        val diameter = size.minDimension
        val radius = diameter / 3f
        val center = Offset(size.width / 2, size.height / 2)
        
        // ===== 关键修复：除零保护 =====
        // 计算总和，如果为0则使用1作为保护值
        val totalSum = timeSumsByMainEvent.values.sum()
        // 使用 coerceAtLeast(1) 确保分母不为0
        val safeTotalSum = totalSum.coerceAtLeast(1L)
        
        var startAngle = -90f

        timeSumsByMainEvent.forEach { (mainEvent, sum) ->
            // 使用安全的除法，避免除零错误
            val sweepAngle = (sum.toFloat() / safeTotalSum.toFloat()) * 360f
            val labelAngle = startAngle + sweepAngle / 2
            val labelOffset = calculateLabelPosition(center, labelAngle, radius + 60f)
            val lineStart = calculateLineStart(center, labelAngle, radius)
            val lineEnd = calculateLineEnd(center, labelAngle, radius + 30f)

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

            // 绘制文本标签（仅当扇形块足够大时显示）
            if (sweepAngle > 15f) {
                drawContext.canvas.nativeCanvas.drawText(
                    mainEvent,
                    labelOffset.x,
                    labelOffset.y,
                    labelPaint
                )
            }

            startAngle += sweepAngle
        }

        // 绘制中间的圆形（使饼图变为环形图，更美观）
        val innerCircleRadius = radius / 2
        drawCircle(
            color = Color.White,
            center = center,
            radius = innerCircleRadius
        )
    }

    // 显示可展开的详细列表
    ExpandableList(timePieces, colorMap)
}

// 标签画笔配置
private val labelPaint = android.graphics.Paint().apply {
    isAntiAlias = true
    textSize = 30f
    color = android.graphics.Color.BLACK
    textAlign = android.graphics.Paint.Align.CENTER
}
