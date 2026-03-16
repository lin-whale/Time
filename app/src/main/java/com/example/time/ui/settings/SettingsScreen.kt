/**
 * SettingsScreen - 设置界面
 * 
 * 功能：
 * 1. 主题切换入口
 * 2. 帮助文档入口
 * 3. 提交确认开关（控制新建事件时是否需要二次确认）
 */
package com.example.time.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.ui.activity.ThemeSelectionActivity

/**
 * 设置项数据模型
 */
sealed class SettingItem {
    data class SwitchItem(
        val title: String,
        val description: String,
        val icon: String,
        val key: String,
        val defaultValue: Boolean = false
    ) : SettingItem()
    
    data class NavigationItem(
        val title: String,
        val description: String,
        val icon: String,
        val onClick: () -> Unit
    ) : SettingItem()
}

/**
 * 设置界面主组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onShowHelp: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("time_app_settings", Context.MODE_PRIVATE) }
    
    // 提交确认开关状态
    var requireSubmitConfirm by remember { 
        mutableStateOf(prefs.getBoolean("require_submit_confirm", true)) 
    }
    
    // 主题选择 Activity launcher
    val themeActivityLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "⚙️ 设置",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== 界面设置分组 =====
            SettingGroupTitle(title = "界面")
            
            SettingNavigationCard(
                title = "主题",
                description = "切换配色方案和深色模式",
                icon = "🎨",
                onClick = {
                    val intent = Intent(context, ThemeSelectionActivity::class.java)
                    themeActivityLauncher.launch(intent)
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ===== 行为设置分组 =====
            SettingGroupTitle(title = "行为")
            
            SettingSwitchCard(
                title = "提交前确认",
                description = "新建事件时显示确认对话框，允许修改结束时间",
                icon = "✓",
                checked = requireSubmitConfirm,
                onCheckedChange = { enabled ->
                    requireSubmitConfirm = enabled
                    prefs.edit().putBoolean("require_submit_confirm", enabled).apply()
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ===== 帮助与关于分组 =====
            SettingGroupTitle(title = "帮助与关于")
            
            SettingNavigationCard(
                title = "帮助文档",
                description = "查看应用使用说明",
                icon = "❓",
                onClick = onShowHelp
            )
        }
    }
}

/**
 * 分组标题
 */
@Composable
fun SettingGroupTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

/**
 * 开关设置卡片
 */
@Composable
fun SettingSwitchCard(
    title: String,
    description: String,
    icon: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Text(
                text = icon,
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            // 标题和描述
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
            
            // 开关
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

/**
 * 导航设置卡片
 */
@Composable
fun SettingNavigationCard(
    title: String,
    description: String,
    icon: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Text(
                text = icon,
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            // 标题和描述
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
            
            // 箭头图标
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
