package com.example.time

//import android.graphics.Color
import android.os.Bundle
import android.util.Log
//import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.InspectableModifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.room.Room
import com.example.time.logic.model.AppDatabase
import com.example.time.logic.model.LifePiece
import com.example.time.logic.model.TimePiece
import com.example.tiptime.ui.theme.TimeTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.concurrent.thread

//
//class MainActivity : AppCompatActivity() {
//    @SuppressLint("MissingInflatedId", "SetTextI18n")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            TimeTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                ) {
//                    TimeAPPLayout()
//                }
//            }
//        }
////        setContentView(R.layout.activity_main)
//        val timePieceDao = AppDatabase.getDatabase(this).timePieceDao()
//
//        // 获取当前时间
////        val currentTimeMillis = System.currentTimeMillis()
//
//
//        val recordEditText = findViewById<EditText>(R.id.recordEditText)
//        val nextText = findViewById<EditText>(R.id.tiYanText)
//        val ratingEditText = findViewById<EditText>(R.id.ratingText)
//        val submitButton = findViewById<Button>(R.id.submitButton)
//        val queryDataBtn: Button = findViewById(R.id.showButton)
//        val previousRecord: TextView = findViewById(R.id.previousRecord)
//
//        submitButton.setOnClickListener {
//            val databaseThread = thread {
//                var record = recordEditText.text.toString()
//                var tiYan = nextText.text.toString()
//                val emotionStr = ratingEditText.text.toString()
//                var mainEvent = ""
//                var subEvent = ""
//
//                val emotion: Int = if (emotionStr.isEmpty()) {
//                    0
//                } else {
//                    emotionStr.toInt()
//                }
//
//                val hierarchyRecord = record.split("[：:]".toRegex())
//                mainEvent = hierarchyRecord[0]
//                if (hierarchyRecord.size > 1) {
//                    subEvent = hierarchyRecord[1]
//                }
//
//                val lastPiece: List<TimePiece> = timePieceDao.getLatestRow()
//                val fromTimePoint: Long = if (lastPiece.isNotEmpty()) {
//                    lastPiece[0].timePoint
//                } else {
//                    System.currentTimeMillis()
//                }
//                val timePiece = TimePiece(
//                    timePoint = System.currentTimeMillis(), fromTimePoint = fromTimePoint,
//                    emotion = emotion, lastTimeRecord = tiYan,
//                    mainEvent = mainEvent, subEvent = subEvent
//                )
//                timePieceDao.insertTimePiece(timePiece)
//
//                // 显示提交的record和时间
//                val date = Date(timePiece.timePoint)
//                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//                sdf.timeZone = TimeZone.getTimeZone("GMT+8")  // 设置时区为东八区
//                val formattedTime = sdf.format(date)
//                previousRecord.text = "$record\n$formattedTime"
//                previousRecord.setTextColor(Color.parseColor("#FFC0CB"))
//            }
//
//            // 清空
//            databaseThread.join()
//            recordEditText.setText("")
//            nextText.setText("")
//            ratingEditText.setText("")
//
//            val date = Date(System.currentTimeMillis())
//            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//            sdf.timeZone = TimeZone.getTimeZone("GMT+8")  // 设置时区为东八区
//            val formattedTime = sdf.format(date)
//
//            val toast = Toast.makeText(
//                applicationContext,
//                "Current time: $formattedTime",
//                Toast.LENGTH_LONG
//            )
//            toast.show()
//        }
//
//        queryDataBtn.setOnClickListener {
//            thread {
//                for (timePiece in timePieceDao.loadAllTimePieces()) {
//                    Log.d("MainActivity", timePiece.toString())
//                }
//            }
//        }
//
//    }
//}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db by lazy {
            Room.databaseBuilder(this, AppDatabase::class.java, "app_database")
                .build()
        }

        setContent {
            TimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TimeAPPLayout(db)
                }
            }
        }
    }
}

fun convertTimeFormat(timePoint: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("GMT+8")  // 设置时区为东八区
    return sdf.format(timePoint)
}

fun convertStringToLong(timeString: String): Long {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("GMT+8")  // 设置时区为东八区
    val date = sdf.parse(timeString)
    return date?.time ?: 0L
}

@OptIn(DelicateCoroutinesApi::class)
fun getPreviousRecordString(db: AppDatabase, callback: (String) -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
        // 在 IO 线程中执行数据库查询操作
        val result =
            if (db.timePieceDao().getCount() == 0) {
                "开始记录生命体验吧~"
            } else if (db.timePieceDao().getLatestRow()[0].subEvent.isNotEmpty()) {
                db.timePieceDao().getLatestRow()[0].mainEvent + ":" + db.timePieceDao()
                    .getLatestRow()[0].subEvent + "\n" + convertTimeFormat(
                    db.timePieceDao().getLatestRow()[0].timePoint
                )
            } else {
                db.timePieceDao().getLatestRow()[0].mainEvent + "\n" + convertTimeFormat(
                    db.timePieceDao().getLatestRow()[0].timePoint
                )
            }

        // 将查询结果传递给主线程中的回调函数
        launch(Dispatchers.Main) {
            callback(result)
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun getPreviousRecord(db: AppDatabase, callback: (TimePiece?) -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
        // 在 IO 线程中执行数据库查询操作
        val result =
            if (db.timePieceDao().getCount() == 0) {
                null
            } else {
                db.timePieceDao().getLatestRow()[0]
            }

        // 将查询结果传递给主线程中的回调函数
        launch(Dispatchers.Main) {
            callback(result)
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun getAllLifePieces(db: AppDatabase, callback: (List<LifePiece>) -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
        // 在 IO 线程中执行数据库查询操作
        val result = db.lifePieceDao().loadAllLifePieces()

        // 将查询结果传递给主线程中的回调函数
        launch(Dispatchers.Main) {
            callback(result)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimeAPPLayout(db: AppDatabase) {
    var previousRecord by remember {
        mutableStateOf("")
    }
    getPreviousRecordString(db) { result ->
        // 在这里处理从数据库获取到的特定字段的值
        previousRecord = result
    }
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

    var lifePieces by remember {
        mutableStateOf(listOf<LifePiece>())
    }
    getAllLifePieces(db) { result ->
        lifePieces = result
    }
    var curTime: Long by remember {
        mutableStateOf(System.currentTimeMillis())
    }
    var latestTime: Long by remember {
        mutableStateOf(System.currentTimeMillis())
    }
    getPreviousRecord(db) { result ->
        latestTime = result?.timePoint ?: System.currentTimeMillis()
    }
    var newEvent by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var isTimePickerOpen by remember { mutableStateOf(false) }

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
            text = previousRecord,
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
            LifeList(lifePieces) { selectedLifePiece ->
                record = selectedLifePiece.lifePiece
            }
        }
        InputField(
            label = R.string.tiYan_prompt,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
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
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                getPreviousRecord(db) { result ->
                    latestTime = result?.timePoint ?: System.currentTimeMillis()
                }
                isTimePickerOpen = true
            }) {
                Text(text = "时光回溯~")
            }

            if (isTimePickerOpen) {
                TimePickerDialog(
                    latestTime = latestTime,
                    onTimeSelected = {
                        curTime = it
                        isTimePickerOpen = false
                    },
                    onCancel = {
                        isTimePickerOpen = false
                    }
                )
            }

            Button(
                onClick = {
                    val databaseThread = thread {
                        curTime = System.currentTimeMillis()
                        val emotion: Int = emotionStar
                        val hierarchyRecord = record.split("[：:]".toRegex())
                        mainEvent = hierarchyRecord[0]
                        if (hierarchyRecord.size > 1) {
                            subEvent = hierarchyRecord[1]
                        }

                        val lastPiece: List<TimePiece> = db.timePieceDao().getLatestRow()
                        val fromTimePoint: Long = if (lastPiece.isNotEmpty()) {
                            lastPiece[0].timePoint
                        } else {
                            System.currentTimeMillis()
                        }
                        val timePiece = TimePiece(
                            timePoint = curTime, fromTimePoint = fromTimePoint,
                            emotion = emotion, lastTimeRecord = tiYan,
                            mainEvent = mainEvent, subEvent = subEvent
                        )
                        if(mainEvent != ""){
                            db.timePieceDao().insertTimePiece(timePiece)
                        }
                    }
                    showDialog = false
                    databaseThread.join()
                    getPreviousRecordString(db) { result ->
                        previousRecord = result
                    }
                    // 清空输入窗口
                    record = ""
                    tiYan = ""
                    emotionStar = 3
                }
            ) {
                Text("✔️")
            }
        }
        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text("Add Event")
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
//                        dbViewModel.insertText(text)
                        val databaseThread = thread {
                            db.lifePieceDao().insert(LifePiece(lifePiece = newEvent))
                        }
                        showDialog = false
                    }) {
                        Text("Confirm")
                    }
                }
            }
        }

//        Button(onClick = {
//            thread {
//                for (timePiece in db.timePieceDao().loadAllTimePieces()) {
//                    Log.d("MainActivity", timePiece.toString())
//                }
//            }
//        }) {
//            Text("Show db")
//        }
    }
}

@Composable
fun LifeButton(lifePiece: LifePiece, onLifeSelected: (LifePiece) -> Unit) {
    Button(
        onClick = { onLifeSelected(lifePiece) },
        modifier = Modifier
            .padding(0.dp)
            .wrapContentSize()
    ) {
        Text(
            text = lifePiece.lifePiece,
            modifier = Modifier
                .wrapContentSize()
                .padding(0.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LifeList(lifeList: List<LifePiece>, onLifeSelected: (LifePiece) -> Unit) {
    FlowRow() {
        lifeList.forEach { lifePiece ->
            LifeButton(lifePiece, onLifeSelected)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    @StringRes label: Int,
    keyboardOptions: KeyboardOptions,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier
) {
    TextField(
        value = value,
        singleLine = false,
        modifier = modifier,
        onValueChange = onValueChanged,
        label = { Text(stringResource(label)) },
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun TimePickerDialog(latestTime: Long, onTimeSelected: (Long) -> Unit, onCancel: () -> Unit) {
    var latestTime: Long by remember { mutableStateOf(latestTime) }
    var selectedTime:Long by remember { mutableStateOf(System.currentTimeMillis()) }
    var ratio by remember { mutableStateOf(0f) }

    AlertDialog(
        onDismissRequest = { /* 点击外部区域关闭对话框 */ },
        title = { Text(text = "选择时间") },
        text = {
            Column {
                Slider(
                    value = ratio,
                    onValueChange = {
                        ratio = it
                        selectedTime = (it * (System.currentTimeMillis() - latestTime) + latestTime).toLong()
                    },
                    valueRange = 0f..1f,
                    steps = 100,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                var timeString = convertTimeFormat(selectedTime).substring(5)
                var latestTimeString = convertTimeFormat(latestTime).substring(5)
                Text(text = "from $latestTimeString to $timeString", fontSize = 16.sp)
            }
        },
        confirmButton = {
            Button(onClick = { onTimeSelected(selectedTime) }) {
                Text(text = "确认")
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text(text = "取消")
            }
        }
    )
}


//@Preview(showBackground = true)
//@Composable
//fun TipTimeLayoutPreview(@PreviewParameter(YourDatabaseProvider::class) db: AppDatabase = TimeApplication().db) {
//    TimeTheme {
//        TimeAPPLayout(db = db)
//    }
//}
//
//class YourDatabaseProvider : PreviewParameterProvider<AppDatabase> {
//    override val values: Sequence<AppDatabase>
//        get() = sequenceOf(TimeApplication().db)
//}