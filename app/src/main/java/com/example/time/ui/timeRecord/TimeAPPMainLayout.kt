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
    var mainEvent = ""
    var subEvent = ""
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
            .padding(horizontal = 40.dp)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
            color = Color(0xFFFFC0CB),
            modifier = Modifier
                .padding(bottom = 16.dp, top = 40.dp)
                .align(alignment = Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )
        InputField(
            label = R.string.previous_event_prompt,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            value = record,
            onValueChanged = { record = it },
            modifier = Modifier
                .padding(bottom = 10.dp)
                .fillMaxWidth()
        )
        FlowRow(
            modifier = Modifier.fillMaxSize()
        ) {
            lifePieces.value?.let {
                LifeList(it) { selectedLifePiece ->
                    record = selectedLifePiece.lifePiece
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
            modifier = Modifier
                .padding(bottom = 10.dp, top = 10.dp)
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier.align(Alignment.End)
        ) {
            repeat(5) { index ->
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = if (index < emotionStar) Color.Yellow else Color.Gray,
                    modifier = Modifier
                        .clickable {
                            // 更新用户评分
                            emotionStar = index + 1
                        }
                        .padding(4.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                isTimePickerOpen = true
            }) {
                Text(text = "时光回溯~")
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
            ) {
                Text("Event")
            }

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBFE9FF)),
                onClick = {
                    // 工作流优化：点击提交后，先显示确认对话框而不是直接提交
                    if (!isTimePick) {
                        curTime = System.currentTimeMillis()
                    }
                    confirmTime = curTime
                    
                    val hierarchyRecord = record.split("[：:]".toRegex())
                    mainEvent = hierarchyRecord[0]
                    if (hierarchyRecord.size > 1) {
                        subEvent = hierarchyRecord[1]
                    }
                    
                    // 只在有主事件时显示确认对话框
                    if (mainEvent != "") {
                        showConfirmDialog = true
                    }
                }
            ) {
                Text("✔️")
            }
        }
        Row {
            Button(onClick = {
                isMDOpen = true
            }) {
                Text(text = "?")
            }
            if(isMDOpen){
                IntroductionDialog{
                    isMDOpen = false
                }
            }
//            ButtonToShowIntroduction()
            ButtonToShowTimePiecesActivity()
            ButtonToShowTimeActivity()
            ButtonToShowFeelingPiecesActivity()
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
                                    tint = if (index < emotionStar) Color.Yellow else Color.Gray,
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
fun ButtonToShowTimeActivity() {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 处理从新的 Activity 返回的结果
        }
        Button(onClick = {
            val intent = Intent(context, ShowTimeActivity::class.java)
            activityResultLauncher.launch(intent)
        }) {
            Text("\uD83D\uDD52")
        }
}

@Composable
fun ButtonToShowFeelingPiecesActivity() {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 处理从新的 Activity 返回的结果
        }

    Button(onClick = {
        val intent = Intent(context, ShowFeelingActivity::class.java)
        activityResultLauncher.launch(intent)
    }) {
        Text("\uD83D\uDC96")
    }
}

@Composable
fun ButtonToShowTimePiecesActivity() {
    val context = LocalContext.current
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 处理从新的 Activity 返回的结果
        }

    Button(onClick = {
        val intent = Intent(context, ShowTimePiecesActivity::class.java)
        activityResultLauncher.launch(intent)
    }) {
        Text("\uD83D\uDCCB")
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


