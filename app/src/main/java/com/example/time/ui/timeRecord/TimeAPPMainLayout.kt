/**
 * TimeAPPMainLayout - 主界面布局
 * 
 * 改动说明：
 * 1. 界面美学优化：
 *    - 使用卡片式布局，层次更分明
 *    - 优化颜色搭配，使用 MaterialTheme
 *    - 添加阴影和圆角，提升视觉效果
 *    - 优化按钮布局和间距
 * 
 * 2. 用户工作流优化：
 *    - 点击提交后显示确认对话框
 *    - 允许用户修改事件结束时间
 *    - 预览完整记录后再确认提交
 * 
 * 开发原则：
 * - 所有数据仅在本地处理，不上传网络
 * - 增强用户交互体验，减少误操作
 */
package com.example.time.ui.timeRecord

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.time.R
import com.example.time.ui.activity.ShowFeelingActivity
import com.example.time.ui.activity.ShowTimeActivity
import com.example.time.logic.model.LifePiece
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertTimeFormat
import com.example.time.ui.TimeViewModel
import com.example.time.ui.activity.ShowIntroductionActivity
import com.example.time.ui.activity.ShowTimePiecesActivity
import com.example.time.ui.showLifePieces.LifePieceListEdit
import com.example.time.ui.theme.EmotionColors
import com.example.time.ui.components.MediaPicker

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimeAPPMainLayout(viewModel: TimeViewModel = viewModel()) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("time_app_settings", Context.MODE_PRIVATE) }
    
    // ===== 状态变量 =====
    var record by remember { mutableStateOf("") }
    var tiYan by remember { mutableStateOf("") }
    var emotionStar by remember { mutableStateOf(3) }
    var mainEvent = ""
    var subEvent = ""
    
    val lifePieces = viewModel.allLifePieces.observeAsState()
    val previousTimePiece = viewModel.previousTimePiece.observeAsState()
    
    var curTime: Long by remember { mutableStateOf(System.currentTimeMillis()) }
    var newEvent by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var isTimePickerOpen by remember { mutableStateOf(false) }
    var isMDOpen by remember { mutableStateOf(false) }
    var isTimePick by remember { mutableStateOf(false) }
    var showDataManage by remember { mutableStateOf(false) } // 数据管理对话框
    
    // 新增：提交确认对话框状态
    var showSubmitConfirm by remember { mutableStateOf(false) }
    var pendingTimePiece by remember { mutableStateOf<TimePiece?>(null) }
    
    // 媒体附件状态
    var mediaPaths by remember { mutableStateOf(emptyList<String>()) }
    
    // Activity launchers
    val themeActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* 不需要处理返回结果 */ }
    
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // ===== 上一事件卡片（美化） =====
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📍 上一事件",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (previousTimePiece.value?.getOrNull(0)?.mainEvent ?: "开始记录生命体验吧~") +
                            (if (previousTimePiece.value?.getOrNull(0)?.subEvent?.isNotEmpty() == true) 
                                "：${previousTimePiece.value?.get(0)?.subEvent}" else ""),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = convertTimeFormat(
                        previousTimePiece.value?.getOrNull(0)?.timePoint ?: System.currentTimeMillis(),
                        "yyyy/MM/dd HH:mm"
                    ),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // ===== 事件输入卡片 =====
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 事件输入框
                InputField(
                    label = R.string.previous_event_prompt,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    value = record,
                    onValueChanged = { record = it },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 快捷标签选择
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 5
                ) {
                    lifePieces.value?.let {
                        LifeList(it) { selectedLifePiece ->
                            record = selectedLifePiece.lifePiece
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))
                
                // 体验输入框
                InputField(
                    label = R.string.tiYan_prompt,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    ),
                    value = tiYan,
                    onValueChanged = { tiYan = it },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 情绪评分（美化）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "心情：",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < emotionStar) 
                                EmotionColors.getColorForStar(emotionStar) 
                            else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { emotionStar = index + 1 }
                                .padding(4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ===== 媒体附件 =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📷 图片附件",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (mediaPaths.isNotEmpty()) {
                                Text(
                                    text = "${mediaPaths.size}张",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        MediaPicker(
                            mediaPaths = mediaPaths,
                            onMediaAdded = { path ->
                                if (mediaPaths.size < 9) {
                                    mediaPaths = mediaPaths + path
                                }
                            },
                            onMediaRemoved = { path ->
                                mediaPaths = mediaPaths - path
                            },
                            maxMedia = 9
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // ===== 操作按钮行 =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 时光回溯按钮
            OutlinedButton(
                onClick = { isTimePickerOpen = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "⏪ 回溯")
            }

            if (isTimePickerOpen) {
                TimePickerDialog(
                    latestTime = previousTimePiece.value?.getOrNull(0)?.timePoint
                        ?: System.currentTimeMillis(),
                    onTimeSelected = {
                        curTime = it
                        isTimePickerOpen = false
                        isTimePick = true
                    },
                    onCancel = {
                        isTimePickerOpen = false
                    }
                )
            }

            // 事件管理按钮
            OutlinedButton(
                onClick = { showDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🏷️ 标签")
            }

            // 提交按钮（优化工作流）
            Button(
                onClick = {
                    if (!isTimePick) {
                        curTime = System.currentTimeMillis()
                    }
                    val emotion: Int = emotionStar
                    val hierarchyRecord = record.split("[：:]".toRegex())
                    mainEvent = hierarchyRecord[0]
                    if (hierarchyRecord.size > 1) {
                        subEvent = hierarchyRecord[1]
                    } else {
                        subEvent = ""
                    }
                    val fromTimePoint: Long =
                        previousTimePiece.value?.getOrNull(0)?.timePoint ?: System.currentTimeMillis()
                    
                    // 构建待提交的 TimePiece
                    val timePiece = TimePiece(
                        timePoint = curTime,
                        fromTimePoint = fromTimePoint,
                        emotion = emotion,
                        lastTimeRecord = tiYan,
                        mainEvent = mainEvent,
                        subEvent = subEvent
                    )
                    // 设置媒体附件
                    timePiece.setMediaList(mediaPaths)
                    
                    if (mainEvent.isNotBlank()) {
                        // 读取设置：是否需要确认对话框
                        val requireConfirm = prefs.getBoolean("require_submit_confirm", true)
                        
                        if (requireConfirm) {
                            // 显示确认对话框
                            pendingTimePiece = timePiece
                            showSubmitConfirm = true
                        } else {
                            // 直接提交
                            viewModel.insertTimePiece(timePiece)
                            
                            // 清空输入
                            record = ""
                            tiYan = ""
                            emotionStar = 3
                            isTimePick = false
                            mediaPaths = emptyList()
                        }
                    }
                },
                modifier = Modifier.weight(1.2f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("✓ 提交", fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // ===== 底部功能按钮 =====
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 设置按钮
                ButtonToShowSettingsActivity()
                
                // 主题设置按钮已隐藏（设置界面中已有主题选择功能）
                // val context = LocalContext.current
                // IconTextButton(
                //     icon = "🎨",
                //     text = "主题",
                //     onClick = {
                //         val intent = Intent(context, com.example.time.ui.activity.ThemeSelectionActivity::class.java)
                //         themeActivityLauncher.launch(intent)
                //     }
                // )
                
                // 记录列表
                ButtonToShowTimePiecesActivity()
                
                // 时间统计
                ButtonToShowTimeActivity()
                
                // 心情统计
                ButtonToShowFeelingPiecesActivity()
                
                // 数据管理
                IconTextButton(
                    icon = "💾",
                    text = "备份",
                    onClick = { showDataManage = true }
                )
            }
        }
        
        // ===== 数据管理对话框 =====
        if (showDataManage) {
            DataManageDialog(
                viewModel = viewModel,
                onDismiss = { showDataManage = false }
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // ===== 提交确认对话框 =====
        if (showSubmitConfirm && pendingTimePiece != null) {
            SubmitConfirmDialog(
                timePiece = pendingTimePiece!!,
                onConfirm = { finishedTime, mediaPaths ->
                    // 使用用户可能修改后的结束时间 + 媒体附件
                    val finalPiece = pendingTimePiece!!.copy(timePoint = finishedTime)
                    finalPiece.setMediaList(mediaPaths)
                    viewModel.insertTimePiece(finalPiece)
                    
                    // 清空输入
                    record = ""
                    tiYan = ""
                    emotionStar = 3
                    isTimePick = false
                    showSubmitConfirm = false
                    pendingTimePiece = null
                },
                onCancel = {
                    showSubmitConfirm = false
                    pendingTimePiece = null
                },
                latestTime = previousTimePiece.value?.getOrNull(0)?.timePoint ?: System.currentTimeMillis()
            )
        }
        
        // ===== 标签管理对话框 =====
        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "🏷️ 管理事件标签",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TextField(
                            value = newEvent,
                            onValueChange = { newEvent = it },
                            label = { Text("新建标签") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = {
                                if (newEvent.isNotBlank()) {
                                    viewModel.insertLifePiece(LifePiece(lifePiece = newEvent))
                                    newEvent = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("➕ 添加标签")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "已有标签（点击删除）",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        lifePieces.value?.let { LifePieceListEdit(it, viewModel) }
                    }
                }
            }
        }
    }
}

/**
 * 图标文字按钮组件
 * 用于底部功能按钮
 */
@Composable
fun IconTextButton(
    icon: String,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ButtonToShowTimeActivity() {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ -> }
    
    IconTextButton(
        icon = "📊",
        text = "时间",
        onClick = {
            val intent = Intent(context, ShowTimeActivity::class.java)
            activityResultLauncher.launch(intent)
        }
    )
}

@Composable
fun ButtonToShowFeelingPiecesActivity() {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ -> }

    IconTextButton(
        icon = "💖",
        text = "心情",
        onClick = {
            val intent = Intent(context, ShowFeelingActivity::class.java)
            activityResultLauncher.launch(intent)
        }
    )
}

@Composable
fun ButtonToShowTimePiecesActivity() {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ -> }

    IconTextButton(
        icon = "📋",
        text = "记录",
        onClick = {
            val intent = Intent(context, ShowTimePiecesActivity::class.java)
            activityResultLauncher.launch(intent)
        }
    )
}

@Composable
fun ButtonToShowIntroduction() {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ -> }

    IconTextButton(
        icon = "📖",
        text = "手册",
        onClick = {
            val intent = Intent(context, ShowIntroductionActivity::class.java)
            activityResultLauncher.launch(intent)
        }
    )
}

@Composable
fun ButtonToShowSettingsActivity() {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ -> }

    IconTextButton(
        icon = "⚙️",
        text = "设置",
        onClick = {
            val intent = Intent(context, com.example.time.ui.activity.SettingsActivity::class.java)
            activityResultLauncher.launch(intent)
        }
    )
}
