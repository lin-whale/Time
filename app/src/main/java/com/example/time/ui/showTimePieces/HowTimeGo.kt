/**
 * HowTimeGo - 情绪分布饼图
 * 
 * 改动说明：
 * - 修复除零错误：当没有记录或总时长为0时，添加保护逻辑
 * - 优化图表显示：空数据时显示友好提示
 * - 使用新的情绪颜色方案
 * 
 * Bug修复说明：
 * - 问题：开始使用时没有记录，计算百分比时 totalSum 为 0 导致除零错误
 * - 解决：在分母中添加保护值，并在空数据时显示提示信息
 */
package com.example.time.ui.showTimePieces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.ui.theme.EmotionColors
import com.example.time.ui.utils.getRandomColor
import kotlin.math.cos
import kotlin.math.sin

/**
 * 情绪分布饼图组件
 * 显示不同心情评分的时间占比
 * 
 * @param timePieces 时间片段列表
 */
@Composable
fun HowTimeGo(timePieces: List<TimePiece>) {
    // ===== 空数据保护 =====
    // 修复：当没有记录时显示友好提示，避免后续计算出错
    if (timePieces.isEmpty()) {
        Box(
            modifier = Modifier
                .size(height = 300.dp, width = 400.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💭",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "暂无心情数据",
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
    
    // Step 1: 计算每种情绪评分的时间长度总和
    val timeSumsByEmotion = timePieces
        .groupBy { it.emotion }
        .mapValues { entry ->
            entry.value.sumOf { it.timePoint - it.fromTimePoint }
        }
        .toList()
        .sortedBy { it.first }
        .toMap()

    // Step 2: 创建颜色映射（使用情绪专属配色）
    val colorMap = mutableMapOf<Int, Color>()
    timeSumsByEmotion.keys.forEach { emotion ->
        // 使用预定义的情绪颜色
        colorMap[emotion] = EmotionColors.getColorForStar(emotion)
    }

    // Step 3: 绘制扇形图、标签和指向线
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
    
    Canvas(modifier = Modifier.size(height = 300.dp, width = 400.dp)) {
        val diameter = size.minDimension
        val radius = diameter / 3f
        val center = Offset(size.width / 2, size.height / 2)
        
        // ===== 关键修复：除零保护 =====
        // 计算总和，如果为0则使用1作为保护值
        val totalSum = timeSumsByEmotion.values.sum()
        // 使用 coerceAtLeast(1) 确保分母不为0
        val safeTotalSum = totalSum.coerceAtLeast(1L)
        
        var startAngle = -90f

        timeSumsByEmotion.forEach { (emotion, sum) ->
            // 使用安全的除法，避免除零错误
            val sweepAngle = (sum.toFloat() / safeTotalSum.toFloat()) * 360f
            val labelAngle = startAngle + sweepAngle / 2
            val labelOffset = calculateLabelPosition(center, labelAngle, radius + 60f)
            val lineStart = calculateLineStart(center, labelAngle, radius)
            val lineEnd = calculateLineEnd(center, labelAngle, radius + 30f)

            // 绘制扇形块
            colorMap[emotion]?.let {
                drawArc(
                    color = it,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = center - Offset(radius, radius),
                    size = Size(radius * 2, radius * 2)
                )
            }

            // 绘制文本标签（用星星表示情绪等级）
            if (sweepAngle > 10f) {
                drawContext.canvas.nativeCanvas.drawText(
                    "⭐".repeat(emotion),
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
}

// 标签画笔配置
private val labelPaint = android.graphics.Paint().apply {
    isAntiAlias = true
    textSize = 30f
    color = android.graphics.Color.BLACK
    textAlign = android.graphics.Paint.Align.CENTER
}
