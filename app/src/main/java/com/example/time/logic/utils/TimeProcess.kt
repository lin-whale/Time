package com.example.time.logic.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

fun convertTimeFormat(timePoint: Long, format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("GMT+8")  // 设置时区为东八区
    return sdf.format(timePoint)
}

fun convertStringToLong(timeString: String, format: String = "yyyy-MM-dd HH:mm:ss"): Long {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("GMT+8")  // 设置时区为东八区
    val date = sdf.parse(timeString)
    return date?.time ?: 0L
}

fun convertDurationFormat(durationInMillis: Long, format: String = "%02d:%02d"): String {
    val hours = TimeUnit.MILLISECONDS.toHours(durationInMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % 60

    return String.format(format, hours, minutes)
}