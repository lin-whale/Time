package com.example.time.ui.utils

import androidx.compose.ui.graphics.Color

fun getContrastingColor(color: Color): Color {
    val darkness = 1 - (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return if (darkness < 0.5) Color.Black else Color.White
//    return Color.Black
}