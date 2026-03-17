package com.example.time.logic.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

fun convertTimeFormat(timePoint: Long, format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("GMT+8")  // 设置时区为东八区
    return sdf.format(timePoint)
}

/**
 * 智能时间格式化：自动判断是否需要显示年份
 * 当时间不属于当年时，自动添加年份显示
 * 
 * @param timePoint 时间戳（毫秒）
 * @param baseFormat 基础格式（不含年份），如 "M/d HH:mm"
 * @return 格式化后的时间字符串
 */
fun convertTimeFormatSmart(timePoint: Long, baseFormat: String = "M/d HH:mm"): String {
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    
    calendar.timeInMillis = timePoint
    val timeYear = calendar.get(Calendar.YEAR)
    
    return if (timeYear != currentYear) {
        // 非当年，添加年份显示
        val yearFormat = "yyyy/$baseFormat"
        convertTimeFormat(timePoint, yearFormat)
    } else {
        // 当年，使用基础格式
        convertTimeFormat(timePoint, baseFormat)
    }
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