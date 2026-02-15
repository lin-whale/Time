package com.example.time.ui.timeRecord

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimeAPPMainLayout(viewModel: TimeViewModel = viewModel()) {
    var record by remember {
        mutableStateOf("")
    }
    var tiYan by remember {
        mutableStateOf("")
    }
    var emotionStar by remember {
        mutableStateOf(3)
    }
    var mainEvent by remember { mutableStateOf("") }
    var subEvent by remember { mutableStateOf("") }
    val lifePieces = viewModel.allLifePieces.observeAsState()
    val previousTimePiece = viewModel.previousTimePiece.observeAsState()
    var curTime: Long by remember {
        mutableStateOf(System.currentTimeMillis())
    }
    var newEvent by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var isTimePickerOpen by remember { mutableStateOf(false) }
    var isMDOpen by remember { mutableStateOf(false) }
    var isTimePick by remember { mutableStateOf(false) }
    // 新增：确认对话框状态
    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var isConfirmTimePickerOpen by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 顶部标题区域 - 优化布局和颜色
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            color = Color(0xFFF3E5F5),
            shadowElevation = 4.dp,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Text(
                text = (previousTimePiece.value?.get(0)?.mainEvent
                    ?: ("开始记录生命体验吧~")) + (if (previousTimePiece.value?.get(0)?.subEvent?.isEmpty() == true) "" else ":${
                    previousTimePiece.value?.get(
                        0
                    )?.subEvent
                }") + "\n" + convertTimeFormat(
                    previousTimePiece.value?.get(0)?.timePoint ?: System.currentTimeMillis()
                ),
                color = Color(0xFF6A1B9A),
                modifier = Modifier
                    .padding(16.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
        }
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
        
        // 生命片段选择区域 - 改进布局
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFFF9E6),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                lifePieces.value?.let {
                    LifeList(it) { selectedLifePiece ->
                        record = selectedLifePiece.lifePiece
                    }
                }
            }
        }
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

        // 情感评分区域 - 优化布局
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFFF3E0),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "情感评分：",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5D4037)
                )
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < emotionStar) Color(0xFFFFD600) else Color(0xFFE0E0E0),
                            modifier = Modifier
                                .clickable {
                                    // 更新用户评分
                                    emotionStar = index + 1
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
        // 操作按钮区域 - 优化布局和样式
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    isTimePickerOpen = true
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1BEE7))
            ) {
                Text(text = "⏰ 时光回溯", color = Color(0xFF4A148C))
            }

            if (isTimePickerOpen) {
                TimePickerDialog(
                    latestTime = previousTimePiece.value?.get(0)?.timePoint
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

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB2DFDB))
            ) {
                Text("📝 事件", color = Color(0xFF004D40))
            }

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF80CBC4)),
                onClick = {
                    // 工作流优化：点击提交后，先显示确认对话框而不是直接提交
                    val hierarchyRecord = record.split("[：:]".toRegex())
                    mainEvent = hierarchyRecord[0]
                    if (hierarchyRecord.size > 1) {
                        subEvent = hierarchyRecord[1]
                    } else {
                        subEvent = ""  // 重置subEvent，避免数据泄露
                    }
                    
                    // 只在有主事件时显示确认对话框
                    if (mainEvent != "") {
                        // 只有在有效事件时才设置时间
                        if (!isTimePick) {
                            curTime = System.currentTimeMillis()
                        }
                        confirmTime = curTime
                        showConfirmDialog = true
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("✅ 提交", color = Color.White)
            }
        }
        // 导航按钮区域 - 改进样式
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    isMDOpen = true
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Text(text = "❓ 帮助", color = Color(0xFF0D47A1))
            }
            if(isMDOpen){
                IntroductionDialog{
                    isMDOpen = false
                }
            }
            
            ButtonToShowTimePiecesActivity(modifier = Modifier.weight(1f))
            ButtonToShowTimeActivity(modifier = Modifier.weight(1f))
            ButtonToShowFeelingPiecesActivity(modifier = Modifier.weight(1f))
        }
        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .graphicsLayer {
                            this.ambientShadowColor = Color.Black
                            shadowElevation = 600.0F
                        }
                ) {
                    TextField(
                        value = newEvent,
                        onValueChange = { newEvent = it },
                        label = { Text("Enter text") }
                    )

                    Button(onClick = {
                        viewModel.insertLifePiece(LifePiece(lifePiece = newEvent))
                        showDialog = false
                    }) {
                        Text("Confirm")
                    }
                    lifePieces.value?.let { LifePieceListEdit(it, viewModel) }
                }
            }
        }

        // 新增：提交确认对话框，允许用户修改结束时间
        if (showConfirmDialog) {
            Dialog(onDismissRequest = { showConfirmDialog = false }) {
                Surface(
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "确认记录",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = "事件：$mainEvent" + (if (subEvent.isEmpty()) "" else "：$subEvent"),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "体验：$tiYan",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("情感：")
                            repeat(5) { index ->
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = if (index < emotionStar) Color(0xFFFFD600) else Color.Gray,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = "结束时间：${convertTimeFormat(confirmTime)}",
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(onClick = {
                                isConfirmTimePickerOpen = true
                            }) {
                                Text(text = "修改时间")
                            }
                            
                            Button(onClick = {
                                showConfirmDialog = false
                            }) {
                                Text("取消")
                            }
                            
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBFE9FF)),
                                onClick = {
                                    // 确认提交：创建TimePiece并保存
                                    val emotion: Int = emotionStar
                                    val fromTimePoint: Long =
                                        previousTimePiece.value?.get(0)?.timePoint ?: System.currentTimeMillis()
                                    val timePiece = TimePiece(
                                        timePoint = confirmTime, 
                                        fromTimePoint = fromTimePoint,
                                        emotion = emotion, 
                                        lastTimeRecord = tiYan,
                                        mainEvent = mainEvent, 
                                        subEvent = subEvent
                                    )
                                    viewModel.insertTimePiece(timePiece)
                                    
                                    // 清空输入窗口
                                    record = ""
                                    tiYan = ""
                                    emotionStar = 3
                                    isTimePick = false
                                    showConfirmDialog = false
                                }
                            ) {
                                Text("确认")
                            }
                        }
                    }
                }
            }
            
            // 确认对话框中的时间选择器
            if (isConfirmTimePickerOpen) {
                TimePickerDialog(
                    latestTime = previousTimePiece.value?.get(0)?.timePoint
                        ?: System.currentTimeMillis(),
                    onTimeSelected = {
                        confirmTime = it
                        isConfirmTimePickerOpen = false
                    },
                    onCancel = {
                        isConfirmTimePickerOpen = false
                    }
                )
            }
        }

//        Button(onClick = {
//            for (timePiece in viewModel.allTimePieces.value!!) {
//                Log.d("MainActivity", timePiece.toString())
//            }
//        }) {
//            Text("Show db")
//        }
    }
}


@Composable
fun ButtonToShowTimeActivity(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 处理从新的 Activity 返回的结果
        }
        Button(
            onClick = {
                val intent = Intent(context, ShowTimeActivity::class.java)
                activityResultLauncher.launch(intent)
            },
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE0B2))
        ) {
            Text("🕒 统计", color = Color(0xFFE65100))
        }
}

@Composable
fun ButtonToShowFeelingPiecesActivity(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 处理从新的 Activity 返回的结果
        }

    Button(
        onClick = {
            val intent = Intent(context, ShowFeelingActivity::class.java)
            activityResultLauncher.launch(intent)
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8BBD0))
    ) {
        Text("💖 感受", color = Color(0xFF880E4F))
    }
}

@Composable
fun ButtonToShowTimePiecesActivity(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 处理从新的 Activity 返回的结果
        }

    Button(
        onClick = {
            val intent = Intent(context, ShowTimePiecesActivity::class.java)
            activityResultLauncher.launch(intent)
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5E1A5))
    ) {
        Text("📋 记录", color = Color(0xFF33691E))
    }
}

@Composable
fun ButtonToShowIntroduction() {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 处理从新的 Activity 返回的结果
        }

    Button(onClick = {
        val intent = Intent(context, ShowIntroductionActivity::class.java)
        activityResultLauncher.launch(intent)
    }) {
        Text("使用手册")
    }
}


