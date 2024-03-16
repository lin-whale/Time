package com.example.time

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.room.Room
import com.example.time.logic.model.AppDatabase
import com.example.time.logic.model.TimePiece
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val timePieceDao = AppDatabase.getDatabase(this).timePieceDao()

        // 获取当前时间
//        val currentTimeMillis = System.currentTimeMillis()


        val recordEditText = findViewById<EditText>(R.id.recordEditText)
        val nextText = findViewById<EditText>(R.id.tiYanText)
        val ratingEditText = findViewById<EditText>(R.id.ratingText)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val queryDataBtn:Button = findViewById(R.id.showButton)
        val previousRecord:TextView = findViewById(R.id.previousRecord)

        submitButton.setOnClickListener {
            val databaseThread = thread{
                var record = recordEditText.text.toString()
                var tiYan = nextText.text.toString()
                val emotionStr = ratingEditText.text.toString()
                var mainEvent = ""
                var subEvent = ""

//                if(tiYan.isEmpty()) {
//                    tiYan = ""
//                }
//                if(record.isEmpty()) {
//                    record = ""
//                }
                val emotion:Int = if(emotionStr.isEmpty()) {
                    0
                }else{
                    emotionStr.toInt()
                }

                val hierarchyRecord = record.split("[：:]".toRegex())
                mainEvent = hierarchyRecord[0]
                if(hierarchyRecord.size > 1){
                    subEvent = hierarchyRecord[1]
                }

                val lastPiece: List<TimePiece> = timePieceDao.getLatestRow()
                val fromTimePoint:Long = if(lastPiece.isNotEmpty()){
                    lastPiece[0].timePoint
                }else{
                    System.currentTimeMillis()
                }
                val timePiece = TimePiece(timePoint = System.currentTimeMillis(), fromTimePoint = fromTimePoint,
                    emotion = emotion, lastTimeRecord = tiYan,
                    mainEvent = mainEvent, subEvent = subEvent)
                timePieceDao.insertTimePiece(timePiece)

                // 显示提交的record和时间
                val date = Date(timePiece.timePoint)
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("GMT+8")  // 设置时区为东八区
                val formattedTime = sdf.format(date)
                previousRecord.text = "$record\n$formattedTime"
                previousRecord.setTextColor(Color.parseColor("#FFC0CB"))
            }

            // 清空
            databaseThread.join()
            recordEditText.setText("")
            nextText.setText("")
            ratingEditText.setText("")

            val date = Date(System.currentTimeMillis())
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("GMT+8")  // 设置时区为东八区
            val formattedTime = sdf.format(date)

            val toast = Toast.makeText(applicationContext, "Current time: $formattedTime", Toast.LENGTH_LONG)
            toast.show()
        }

        queryDataBtn.setOnClickListener {
            thread {
                for (timePiece in timePieceDao.loadAllTimePieces()) {
                    Log.d("MainActivity", timePiece.toString())
                }
            }
        }

    }
}