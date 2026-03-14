/**
 * Modern Bottom Navigation - 现代化底部导航栏
 * 
 * 特点：
 * - 毛玻璃效果
 * - 流畅的切换动画
 * - 清晰的图标和文字
 */
package com.example.time.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.ui.theme.ModernColors
import com.example.time.ui.theme.ModernSizes

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun ModernBottomNavigation(
    items: List<BottomNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(
                    topStart = ModernSizes.CornerLarge.dp,
                    topEnd = ModernSizes.CornerLarge.dp
                ),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        color = Color.White,
        shape = RoundedCornerShape(
            topStart = ModernSizes.CornerLarge.dp,
            topEnd = ModernSizes.CornerLarge.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                BottomNavButton(
                    item = item,
                    selected = selectedRoute == item.route,
                    onClick = { onItemSelected(item.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomNavButton(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = if (selected) ModernColors.Primary else ModernColors.TextTertiary,
        animationSpec = tween(300),
        label = "color"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 图标背景（选中时显示）
        if (selected) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ModernColors.Primary.copy(alpha = 0.15f),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = color,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                )
            }
        } else {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = color,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .size(24.dp)
            )
        }
        
        Text(
            text = item.label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = color
        )
    }
}

/**
 * 现代化浮动操作按钮
 */
@Composable
fun ModernFAB(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    text: String? = null,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.shadow(
            elevation = ModernSizes.ElevationLarge.dp,
            shape = RoundedCornerShape(ModernSizes.CornerMedium.dp),
            spotColor = ModernColors.Primary.copy(alpha = 0.3f)
        ),
        containerColor = ModernColors.Primary,
        contentColor = Color.White,
        shape = RoundedCornerShape(ModernSizes.CornerMedium.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (text != null) 20.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )
            
            if (text != null) {
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 现代化页面标题
 */
@Composable
fun ModernPageHeader(
    title: String,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ModernColors.TextPrimary
            )
            
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = ModernColors.TextSecondary
                )
            }
        }
        
        action?.invoke()
    }
}
