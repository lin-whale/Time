/**
 * Modern Components - 现代化通用组件
 * 
 * 包含：
 * - 现代化按钮（主要、次要、轮廓、文本）
 * - 现代化输入框
 * - 现代化标签
 * - 加载状态
 */
package com.example.time.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.ui.theme.ModernColors
import com.example.time.ui.theme.ModernSizes

/**
 * 现代化主按钮（带渐变色背景）
 */
@Composable
fun ModernPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = ModernColors.GradientPurple
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .shadow(
                elevation = if (enabled) ModernSizes.ElevationMedium.dp else 0.dp,
                shape = RoundedCornerShape(ModernSizes.CornerMedium.dp),
                spotColor = ModernColors.Primary.copy(alpha = 0.3f)
            ),
        enabled = enabled,
        shape = RoundedCornerShape(ModernSizes.CornerMedium.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = ModernColors.SurfaceVariant
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (enabled) gradient else Brush.linearGradient(
                    colors = listOf(ModernColors.SurfaceVariant, ModernColors.SurfaceVariant)
                )),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) Color.White else ModernColors.TextTertiary
            )
        }
    }
}

/**
 * 现代化次要按钮（轮廓样式）
 */
@Composable
fun ModernSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(ModernSizes.CornerMedium.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = SolidColor(if (enabled) ModernColors.Primary else ModernColors.TextTertiary)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = ModernColors.Primary,
            disabledContentColor = ModernColors.TextTertiary
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 现代化输入框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = placeholder,
                color = ModernColors.TextTertiary,
                fontSize = 15.sp
            )
        },
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = TextStyle(
            fontSize = 15.sp,
            color = ModernColors.TextPrimary,
            fontWeight = FontWeight.Normal
        ),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = ModernColors.SurfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(ModernSizes.CornerMedium.dp),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

/**
 * 现代化标签
 */
@Composable
fun ModernChip(
    text: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    color: Color = ModernColors.Primary
) {
    Surface(
        onClick = onClick ?: {},
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) color.copy(alpha = 0.2f) else ModernColors.SurfaceVariant,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) color else ModernColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * 加载指示器
 */
@Composable
fun ModernLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = ModernColors.Primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )
    
    CircularProgressIndicator(
        modifier = modifier.size(24.dp),
        color = color,
        strokeWidth = 3.dp
    )
}

/**
 * 空状态占位
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ModernSizes.SpaceLarge.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon?.invoke()
        
        if (icon != null) {
            Spacer(modifier = Modifier.height(ModernSizes.SpaceMedium.dp))
        }
        
        Text(
            text = message,
            fontSize = 15.sp,
            color = ModernColors.TextSecondary,
            fontWeight = FontWeight.Normal
        )
    }
}
