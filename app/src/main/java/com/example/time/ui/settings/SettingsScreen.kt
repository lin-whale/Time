/**
 * SettingsScreen - 设置界面
 * 
 * 功能：
 * 1. 主题切换入口
 * 2. 帮助文档入口
 * 3. 提交确认开关（控制新建事件时是否需要二次确认）
 * 4. 心情Emoji设置
 */
package com.example.time.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.time.logic.utils.EmojiConfig
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
    
    // Emoji设置对话框状态
    var showEmojiSettings by remember { mutableStateOf(false) }
    var currentEmojis by remember { mutableStateOf(EmojiConfig.getAllEmojis(context)) }
    
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
            
            // ===== 数据显示分组 =====
            SettingGroupTitle(title = "数据显示")
            
            SettingNavigationCard(
                title = "心情表情",
                description = "自定义各心情等级的表情：${currentEmojis.joinToString(" ")}",
                icon = "😊",
                onClick = { showEmojiSettings = true }
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
    
    // ===== Emoji设置对话框 =====
    if (showEmojiSettings) {
        EmojiSettingsDialog(
            currentEmojis = currentEmojis,
            onDismiss = { showEmojiSettings = false },
            onEmojiChange = { feeling, emoji ->
                EmojiConfig.setEmoji(context, feeling, emoji)
                currentEmojis = EmojiConfig.getAllEmojis(context)
            },
            onReset = {
                EmojiConfig.resetToDefault(context)
                currentEmojis = EmojiConfig.getAllEmojis(context)
            }
        )
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

/**
 * 心情Emoji设置对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiSettingsDialog(
    currentEmojis: List<String>,
    onDismiss: () -> Unit,
    onEmojiChange: (feeling: Int, emoji: String) -> Unit,
    onReset: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentLabels by remember { mutableStateOf(EmojiConfig.getAllLabels(context)) }
    var selectedFeeling by remember { mutableStateOf(1) }
    var editingLabel by remember { mutableStateOf(false) }
    var labelText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "😊 心情设置",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 当前心情等级选择
                Text(
                    text = "选择心情等级",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 心情等级按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..5).forEach { feeling ->
                        val isSelected = selectedFeeling == feeling
                        val index = feeling - 1
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFeeling = feeling },
                            label = { 
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(currentEmojis[index], fontSize = 18.sp)
                                    Text(currentLabels[index], fontSize = 11.sp)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 当前选中项
                val selectedIndex = selectedFeeling - 1
                Text(
                    text = "当前：${currentEmojis[selectedIndex]} ${currentLabels[selectedIndex]}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 文字标签编辑
                Text(
                    text = "文字表述",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                OutlinedTextField(
                    value = if (editingLabel) labelText else currentLabels[selectedIndex],
                    onValueChange = { labelText = it },
                    label = { Text("心情${selectedFeeling}级的文字") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (editingLabel && labelText.isNotBlank()) {
                            IconButton(onClick = {
                                val newLabels = currentLabels.toMutableList()
                                newLabels[selectedIndex] = labelText
                                currentLabels = newLabels
                                EmojiConfig.setLabel(context, selectedFeeling, labelText)
                                editingLabel = false
                            }) {
                                Icon(Icons.Default.Check, "保存")
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Emoji选择
                Text(
                    text = "选择表情",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Emoji选择网格
                LazyColumn(
                    modifier = Modifier.height(150.dp)
                ) {
                    items(EmojiConfig.AVAILABLE_EMOJIS.chunked(6)) { emojiRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            emojiRow.forEach { emoji ->
                                TextButton(
                                    onClick = { onEmojiChange(selectedFeeling, emoji) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Text(
                                        text = emoji,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        },
        dismissButton = {
            TextButton(onClick = onReset) {
                Text("重置默认")
            }
        }
    )
}
