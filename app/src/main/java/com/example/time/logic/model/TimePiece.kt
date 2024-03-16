package com.example.time.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TimePiece(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var timePoint: Long,
    var fromTimePoint: Long,
    var emotion: Int,
    var lastTimeRecord: String,
    var mainEvent: String,
    var subEvent: String
)