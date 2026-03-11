/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 颜色主题配置文件
 * 
 * 改动说明：
 * - 优化了整体配色方案，采用更现代、柔和的渐变色系
 * - 主色调改为温暖的珊瑚粉色系，更符合"生命体验记录"的温馨主题
 * - 增强了明暗主题的对比度，提升可读性
 * - 添加了额外的辅助颜色变量，用于UI美化
 */
package com.example.time.ui.theme

import androidx.compose.ui.graphics.Color

// ==================== 浅色主题配色 ====================

// 主色调 - 温暖珊瑚粉（代表生命的温度）
val md_theme_light_primary = Color(0xFFE07A5F)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFFFDAD3)
val md_theme_light_onPrimaryContainer = Color(0xFF3D0800)

// 次要色调 - 柔和紫色（代表思考与体验）
val md_theme_light_secondary = Color(0xFF7B6BA6)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFECDCFF)
val md_theme_light_onSecondaryContainer = Color(0xFF26145C)

// 第三色调 - 清新蓝绿（代表时间流动）
val md_theme_light_tertiary = Color(0xFF3D8E83)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFBDF2E7)
val md_theme_light_onTertiaryContainer = Color(0xFF00201C)

// 错误色
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)

// 背景与表面
val md_theme_light_background = Color(0xFFFFFBF9)
val md_theme_light_onBackground = Color(0xFF201A19)
val md_theme_light_surface = Color(0xFFFFFBF9)
val md_theme_light_onSurface = Color(0xFF201A19)
val md_theme_light_surfaceVariant = Color(0xFFF5DDDA)
val md_theme_light_onSurfaceVariant = Color(0xFF534341)

// 边框与装饰
val md_theme_light_outline = Color(0xFF857371)
val md_theme_light_inverseOnSurface = Color(0xFFFBEEEB)
val md_theme_light_inverseSurface = Color(0xFF362F2E)
val md_theme_light_inversePrimary = Color(0xFFFFB4A5)
val md_theme_light_surfaceTint = Color(0xFFE07A5F)
val md_theme_light_outlineVariant = Color(0xFFD8C2BE)
val md_theme_light_scrim = Color(0xFF000000)

// ==================== 深色主题配色 ====================

// 主色调
val md_theme_dark_primary = Color(0xFFFFB4A5)
val md_theme_dark_onPrimary = Color(0xFF5E1508)
val md_theme_dark_primaryContainer = Color(0xFF7D2E1B)
val md_theme_dark_onPrimaryContainer = Color(0xFFFFDAD3)

// 次要色调
val md_theme_dark_secondary = Color(0xFFD5BAFF)
val md_theme_dark_onSecondary = Color(0xFF3C2A73)
val md_theme_dark_secondaryContainer = Color(0xFF54428B)
val md_theme_dark_onSecondaryContainer = Color(0xFFECDCFF)

// 第三色调
val md_theme_dark_tertiary = Color(0xFFA1D6CB)
val md_theme_dark_onTertiary = Color(0xFF063731)
val md_theme_dark_tertiaryContainer = Color(0xFF254E48)
val md_theme_dark_onTertiaryContainer = Color(0xFFBDF2E7)

// 错误色
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

// 背景与表面
val md_theme_dark_background = Color(0xFF201A19)
val md_theme_dark_onBackground = Color(0xFFEDE0DD)
val md_theme_dark_surface = Color(0xFF201A19)
val md_theme_dark_onSurface = Color(0xFFEDE0DD)
val md_theme_dark_surfaceVariant = Color(0xFF534341)
val md_theme_dark_onSurfaceVariant = Color(0xFFD8C2BE)

// 边框与装饰
val md_theme_dark_outline = Color(0xFFA08C89)
val md_theme_dark_inverseOnSurface = Color(0xFF201A19)
val md_theme_dark_inverseSurface = Color(0xFFEDE0DD)
val md_theme_dark_inversePrimary = Color(0xFFB14629)
val md_theme_dark_surfaceTint = Color(0xFFFFB4A5)
val md_theme_dark_outlineVariant = Color(0xFF534341)
val md_theme_dark_scrim = Color(0xFF000000)

// ==================== 自定义辅助颜色 ====================

/**
 * 情绪星级颜色（用于情感评分显示）
 * 从低到高渐变：灰 → 蓝 → 绿 → 橙 → 金
 */
object EmotionColors {
    val star1 = Color(0xFF9E9E9E)  // 1星 - 灰色（低落）
    val star2 = Color(0xFF64B5F6)  // 2星 - 蓝色（平静）
    val star3 = Color(0xFF81C784)  // 3星 - 绿色（正常）
    val star4 = Color(0xFFFFB74D)  // 4星 - 橙色（愉快）
    val star5 = Color(0xFFFFD54F)  // 5星 - 金色（兴奋）
    
    // 根据星级获取颜色
    fun getColorForStar(star: Int): Color = when(star) {
        1 -> star1
        2 -> star2
        3 -> star3
        4 -> star4
        5 -> star5
        else -> star3
    }
}

/**
 * 卡片渐变背景色
 */
object CardGradientColors {
    val warmStart = Color(0xFFFFF5F3)
    val warmEnd = Color(0xFFFFE8E3)
    val coolStart = Color(0xFFF3F8FF)
    val coolEnd = Color(0xFFE3EEFF)
}

/**
 * 图表配色（用于饼图等统计图表）
 * 使用高对比度、易区分的颜色
 */
object ChartColors {
    val palette = listOf(
        Color(0xFFE07A5F),  // 珊瑚红
        Color(0xFF7B6BA6),  // 薰衣草紫
        Color(0xFF3D8E83),  // 青绿
        Color(0xFFF2CC8F),  // 暖黄
        Color(0xFF81B29A),  // 薄荷绿
        Color(0xFFE87A63),  // 橙红
        Color(0xFF8FA6CB),  // 雾蓝
        Color(0xFFC9A9A6),  // 玫瑰灰
        Color(0xFFB5C99A),  // 草绿
        Color(0xFFD4A373),  // 驼色
    )
    
    // 根据索引获取颜色（循环使用）
    fun getColor(index: Int): Color = palette[index % palette.size]
}
