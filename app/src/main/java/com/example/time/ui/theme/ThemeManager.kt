package com.example.time.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 主题数据类
 */
data class AppTheme(
    val id: String,
    val name: String,
    val primaryLight: Color,
    val primaryDark: Color,
    val secondaryLight: Color,
    val secondaryDark: Color,
    val description: String = ""
)

/**
 * 预设主题列表
 */
object PresetThemes {
    val BLUE = AppTheme(
        id = "blue",
        name = "海洋蓝",
        primaryLight = Color(0xFF0D47A1),
        primaryDark = Color(0xFF42A5F5),
        secondaryLight = Color(0xFF1976D2),
        secondaryDark = Color(0xFF64B5F6),
        description = "清新的蓝色，适合专注工作"
    )
    
    val GREEN = AppTheme(
        id = "green",
        name = "森林绿",
        primaryLight = Color(0xFF2E7D32),
        primaryDark = Color(0xFF66BB6A),
        secondaryLight = Color(0xFF388E3C),
        secondaryDark = Color(0xFF81C784),
        description = "自然的绿色，舒缓眼睛"
    )
    
    val PURPLE = AppTheme(
        id = "purple",
        name = "优雅紫",
        primaryLight = Color(0xFF6A1B9A),
        primaryDark = Color(0xFFAB47BC),
        secondaryLight = Color(0xFF7B1FA2),
        secondaryDark = Color(0xFFBA68C8),
        description = "神秘的紫色，激发创意"
    )
    
    val ORANGE = AppTheme(
        id = "orange",
        name = "活力橙",
        primaryLight = Color(0xFFE65100),
        primaryDark = Color(0xFFFF9800),
        secondaryLight = Color(0xFFF57C00),
        secondaryDark = Color(0xFFFFB74D),
        description = "温暖的橙色，充满活力"
    )
    
    val PINK = AppTheme(
        id = "pink",
        name = "樱花粉",
        primaryLight = Color(0xFFC2185B),
        primaryDark = Color(0xFFF06292),
        secondaryLight = Color(0xFFD81B60),
        secondaryDark = Color(0xFFF48FB1),
        description = "浪漫的粉色，温柔可爱"
    )
    
    val TEAL = AppTheme(
        id = "teal",
        name = "青瓷蓝",
        primaryLight = Color(0xFF00796B),
        primaryDark = Color(0xFF4DB6AC),
        secondaryLight = Color(0xFF00897B),
        secondaryDark = Color(0xFF80CBC4),
        description = "典雅的青色，静谧沉稳"
    )
    
    val INDIGO = AppTheme(
        id = "indigo",
        name = "靛蓝",
        primaryLight = Color(0xFF283593),
        primaryDark = Color(0xFF5C6BC0),
        secondaryLight = Color(0xFF303F9F),
        secondaryDark = Color(0xFF7986CB),
        description = "深邃的靛蓝，专业可靠"
    )
    
    val RED = AppTheme(
        id = "red",
        name = "热情红",
        primaryLight = Color(0xFFC62828),
        primaryDark = Color(0xFFEF5350),
        secondaryLight = Color(0xFFD32F2F),
        secondaryDark = Color(0xFFE57373),
        description = "激情的红色，充满动力"
    )

    val ALL_THEMES = listOf(
        BLUE, GREEN, PURPLE, ORANGE,
        PINK, TEAL, INDIGO, RED
    )
    
    fun getById(id: String): AppTheme {
        return ALL_THEMES.find { it.id == id } ?: BLUE
    }
}

/**
 * 主题管理器 - 单例
 */
object ThemeManager {
    private val _currentTheme = MutableStateFlow(PresetThemes.BLUE)
    val currentTheme: StateFlow<AppTheme> = _currentTheme
    
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode
    
    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
    }
    
    fun setThemeById(id: String) {
        _currentTheme.value = PresetThemes.getById(id)
    }
    
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }
    
    @Composable
    fun createColorScheme(darkTheme: Boolean): ColorScheme {
        val theme by currentTheme.collectAsState()
        
        return if (darkTheme) {
            darkColorScheme(
                primary = theme.primaryDark,
                secondary = theme.secondaryDark,
                tertiary = theme.primaryDark.copy(alpha = 0.7f),
                background = Color(0xFF121212),
                surface = Color(0xFF1E1E1E),
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFFE0E0E0),
                onSurface = Color(0xFFE0E0E0)
            )
        } else {
            lightColorScheme(
                primary = theme.primaryLight,
                secondary = theme.secondaryLight,
                tertiary = theme.primaryLight.copy(alpha = 0.7f),
                background = Color(0xFFFAFAFA),
                surface = Color.White,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFF1C1C1C),
                onSurface = Color(0xFF1C1C1C)
            )
        }
    }
}
