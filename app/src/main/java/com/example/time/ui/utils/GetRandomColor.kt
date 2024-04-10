package com.example.time.ui.utils

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

// 辅助函数：获取随机颜色
fun getRandomColor(): Color {
    val random = Random
    return Color(
        red = random.nextFloat(),
        green = random.nextFloat(),
        blue = random.nextFloat(),
        alpha = 1f
    )
}