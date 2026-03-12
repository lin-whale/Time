/**
 * Modern Theme - 现代化设计主题
 * 
 * 设计理念：
 * - 柔和的配色（降低视觉疲劳）
 * - 大圆角（更友好的视觉体验）
 * - 渐变色（增加层次感）
 * - 微妙的阴影（增强立体感）
 */
package com.example.time.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 现代化配色方案
 * 参考：iOS Human Interface Guidelines + Material You
 */
object ModernColors {
    // ===== 主色调（柔和的蓝紫色系）=====
    val Primary = Color(0xFF6C63FF)  // 柔和的紫色
    val PrimaryLight = Color(0xFF9D97FF)
    val PrimaryDark = Color(0xFF4338CA)
    
    // ===== 背景色（浅灰色系，护眼）=====
    val Background = Color(0xFFF8F9FA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF0F1F3)
    
    // ===== 文字颜色 =====
    val TextPrimary = Color(0xFF1F2937)
    val TextSecondary = Color(0xFF6B7280)
    val TextTertiary = Color(0xFF9CA3AF)
    
    // ===== 功能色 =====
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Info = Color(0xFF3B82F6)
    
    // ===== 情绪色彩（5星评分）=====
    val Emotion1 = Color(0xFFEF4444)  // 很差 - 红色
    val Emotion2 = Color(0xFFF59E0B)  // 差 - 橙色
    val Emotion3 = Color(0xFFFBBF24)  // 一般 - 黄色
    val Emotion4 = Color(0xFF10B981)  // 好 - 绿色
    val Emotion5 = Color(0xFF8B5CF6)  // 很好 - 紫色
    
    fun getEmotionColor(rating: Int): Color {
        return when (rating) {
            1 -> Emotion1
            2 -> Emotion2
            3 -> Emotion3
            4 -> Emotion4
            5 -> Emotion5
            else -> TextTertiary
        }
    }
    
    // ===== 渐变色（用于卡片背景）=====
    val GradientPurple = Brush.linearGradient(
        colors = listOf(
            Color(0xFF667EEA),
            Color(0xFF764BA2)
        )
    )
    
    val GradientBlue = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4F46E5),
            Color(0xFF06B6D4)
        )
    )
    
    val GradientGreen = Brush.linearGradient(
        colors = listOf(
            Color(0xFF059669),
            Color(0xFF10B981)
        )
    )
    
    val GradientOrange = Brush.linearGradient(
        colors = listOf(
            Color(0xFFF97316),
            Color(0xFFFBBF24)
        )
    )
    
    val GradientPink = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEC4899),
            Color(0xFFF472B6)
        )
    )
}

/**
 * 现代化尺寸规范
 */
object ModernSizes {
    // ===== 圆角 =====
    val CornerSmall = 8
    val CornerMedium = 16
    val CornerLarge = 24
    val CornerXLarge = 32
    
    // ===== 间距 =====
    val SpaceXSmall = 4
    val SpaceSmall = 8
    val SpaceMedium = 16
    val SpaceLarge = 24
    val SpaceXLarge = 32
    
    // ===== 阴影 =====
    val ElevationSmall = 2
    val ElevationMedium = 4
    val ElevationLarge = 8
}

/**
 * 现代化动画时长（毫秒）
 */
object ModernAnimations {
    const val Fast = 150
    const val Normal = 300
    const val Slow = 500
}
