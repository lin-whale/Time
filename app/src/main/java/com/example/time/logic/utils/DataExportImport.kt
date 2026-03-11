/**
 * DataExportImport - 数据导入导出工具
 * 
 * 功能说明：
 * - 将时间记录导出为 JSON 文件（支持版本迁移）
 * - 从 JSON 文件导入时间记录
 * - 支持增量导入和覆盖导入
 * 
 * 数据格式版本：v1
 * - 包含版本号便于未来迁移
 * - 包含导出时间戳
 * - 包含完整的 TimePiece 和 LifePiece 数据
 * 
 * 开发原则：
 * - 所有数据仅在本地处理，不上传网络
 * - 支持数据无缝迁移
 */
package com.example.time.logic.utils

import android.content.Context
import android.net.Uri
import com.example.time.logic.model.LifePiece
import com.example.time.logic.model.TimePiece
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * 数据导出格式版本
 * 用于未来的数据迁移兼容性
 */
const val DATA_FORMAT_VERSION = 1

/**
 * 数据导出结果
 */
data class ExportResult(
    val success: Boolean,
    val message: String,
    val filePath: String? = null
)

/**
 * 数据导入结果
 */
data class ImportResult(
    val success: Boolean,
    val message: String,
    val timePiecesCount: Int = 0,
    val lifePiecesCount: Int = 0
)

/**
 * 导出数据包
 */
data class ExportData(
    val version: Int,
    val exportTime: Long,
    val timePieces: List<TimePiece>,
    val lifePieces: List<LifePiece>
)

/**
 * 将时间记录数据导出为 JSON 格式
 * 
 * @param timePieces 时间片段列表
 * @param lifePieces 生活片段标签列表
 * @return JSON 字符串
 */
fun exportToJson(timePieces: List<TimePiece>, lifePieces: List<LifePiece>): String {
    val root = JSONObject()
    
    // 添加元数据
    root.put("version", DATA_FORMAT_VERSION)
    root.put("exportTime", System.currentTimeMillis())
    root.put("appName", "TimeApp")
    
    // 导出时间片段
    val timePiecesArray = JSONArray()
    timePieces.forEach { piece ->
        val pieceJson = JSONObject().apply {
            put("id", piece.id)
            put("timePoint", piece.timePoint)
            put("fromTimePoint", piece.fromTimePoint)
            put("emotion", piece.emotion)
            put("lastTimeRecord", piece.lastTimeRecord)
            put("mainEvent", piece.mainEvent)
            put("subEvent", piece.subEvent)
        }
        timePiecesArray.put(pieceJson)
    }
    root.put("timePieces", timePiecesArray)
    
    // 导出生活片段标签
    val lifePiecesArray = JSONArray()
    lifePieces.forEach { piece ->
        val pieceJson = JSONObject().apply {
            put("id", piece.id)
            put("lifePiece", piece.lifePiece)
        }
        lifePiecesArray.put(pieceJson)
    }
    root.put("lifePieces", lifePiecesArray)
    
    return root.toString(2) // 格式化输出，缩进2空格
}

/**
 * 从 JSON 字符串解析数据
 * 支持版本迁移
 * 
 * @param jsonString JSON 字符串
 * @return 解析后的数据，如果解析失败返回 null
 */
fun parseFromJson(jsonString: String): ExportData? {
    return try {
        val root = JSONObject(jsonString)
        
        // 读取版本号
        val version = root.optInt("version", 1)
        val exportTime = root.optLong("exportTime", 0)
        
        // 解析时间片段
        val timePieces = mutableListOf<TimePiece>()
        val timePiecesArray = root.optJSONArray("timePieces")
        if (timePiecesArray != null) {
            for (i in 0 until timePiecesArray.length()) {
                val pieceJson = timePiecesArray.getJSONObject(i)
                val piece = TimePiece(
                    id = pieceJson.optLong("id", 0),
                    timePoint = pieceJson.getLong("timePoint"),
                    fromTimePoint = pieceJson.getLong("fromTimePoint"),
                    emotion = pieceJson.getInt("emotion"),
                    lastTimeRecord = pieceJson.optString("lastTimeRecord", ""),
                    mainEvent = pieceJson.getString("mainEvent"),
                    subEvent = pieceJson.optString("subEvent", "")
                )
                timePieces.add(piece)
            }
        }
        
        // 解析生活片段标签
        val lifePieces = mutableListOf<LifePiece>()
        val lifePiecesArray = root.optJSONArray("lifePieces")
        if (lifePiecesArray != null) {
            for (i in 0 until lifePiecesArray.length()) {
                val pieceJson = lifePiecesArray.getJSONObject(i)
                val piece = LifePiece(
                    id = pieceJson.optLong("id", 0),
                    lifePiece = pieceJson.getString("lifePiece")
                )
                lifePieces.add(piece)
            }
        }
        
        // 版本迁移处理（为未来扩展预留）
        when (version) {
            1 -> {
                // 当前版本，无需迁移
            }
            // 未来版本可以在这里添加迁移逻辑
            // 2 -> { 迁移 v2 到 v1 或直接处理 }
        }
        
        ExportData(
            version = version,
            exportTime = exportTime,
            timePieces = timePieces,
            lifePieces = lifePieces
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 将数据写入文件（通过 Uri）
 * 
 * @param context 上下文
 * @param uri 文件 Uri
 * @param jsonData JSON 数据字符串
 * @return 是否成功
 */
fun writeToUri(context: Context, uri: Uri, jsonData: String): Boolean {
    return try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                writer.write(jsonData)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * 从文件读取数据（通过 Uri）
 * 
 * @param context 上下文
 * @param uri 文件 Uri
 * @return JSON 字符串，如果读取失败返回 null
 */
fun readFromUri(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                reader.readText()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 生成默认的导出文件名
 * 格式：TimeApp_备份_yyyyMMdd_HHmmss.json
 */
fun generateExportFileName(): String {
    val timestamp = convertTimeFormat(System.currentTimeMillis(), "yyyyMMdd_HHmmss")
    return "TimeApp_备份_$timestamp.json"
}
