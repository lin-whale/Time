package com.example.time.logic.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LifePiece(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var lifePiece: String
)